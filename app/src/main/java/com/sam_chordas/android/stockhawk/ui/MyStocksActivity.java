package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Const;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;

import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    boolean isConnected;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.empty_stocks_nodata_textview) TextView emptyDataTextView;
    @BindView(R.id.empty_stocks_reason_textview) TextView emptyDataReasonTextView;
    @BindView(R.id.empty_stocks_linearlayout) LinearLayout emptyDataLinearLayout;
    @BindView(R.id.my_stocks_coordinatorlayout) CoordinatorLayout myStocksCoordinatorLayout;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.my_stocks_toolbar) Toolbar toolbar;

    @BindString(R.string.unexpected_err_msg) String STRING_UNEXPECTED_ERR_MSG;
    @BindString(R.string.network_status) String STRING_NETWORK_STATUS;
    @BindString(R.string.no_data_present) String STRING_NO_DATA_PRESENT ;
    @BindString(R.string.no_stocks_added) String STRING_NO_STOCKS_ADDED;
    @BindString(R.string.no_network) String STRING_NO_NETWORK;
    @BindString(R.string.malformed_response) String STRING_MALFORMED_RESPONSE;
    @BindString(R.string.server_down) String STRING_SERVER_DOWN;
    @BindString(R.string.internal_server_error) String STRING_INTERNAL_SERVER_ERROR;
    @BindString(R.string.network_toast) String STRING_NETWORK_TOAST;


    StockTaskService stockTaskService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_my_stocks);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        isConnected = Utils.isNetworkConnected(this);

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);

        if (savedInstanceState == null) {
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            if (isConnected) {
                startService(mServiceIntent);
            } else {
                Utils.showSnackbar(myStocksCoordinatorLayout, R.string.network_toast, Utils.SNACKBAR_TYPE_ERROR);
            }
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        Intent stockGraphActivityIntent = new Intent(getApplicationContext(), StockGraphActivity.class);
                        String symbol = ((TextView) v.findViewById(R.id.stock_symbol)).getText().toString();

                        stockGraphActivityIntent.putExtra(Const.EXTRA_STOCK_SYMBOL, symbol);
                        startActivity(stockGraphActivityIntent);
                    }
                }));
        recyclerView.setAdapter(mCursorAdapter);

        //fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't already exist
                                    // in the DB and proceed accordingly
                                    Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                            new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                            new String[]{input.toString().toUpperCase()}, null);
                                    if (c.getCount() != 0) {
                                        Utils.showSnackbar(recyclerView, R.string.stock_already_saved, Utils.SNACKBAR_TYPE_INFO);
                                        return;
                                    } else {
                                        // Add the stock to DB
                                        mServiceIntent.putExtra("tag", "add");
                                        mServiceIntent.putExtra("symbol", input.toString().toUpperCase());
                                        startService(mServiceIntent);
                                    }
                                }
                            })
                            .show();
                } else {
                    Utils.showSnackbar(v, R.string.network_toast, Utils.SNACKBAR_TYPE_ERROR);
                }

            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        setEmptyViewUI(true);
        mTitle = getTitle();
        if (isConnected) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();
            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
    }


    public void setEmptyViewUI(boolean forceSetEmpty) {
        if (forceSetEmpty) {
            emptyDataReasonTextView.setText(STRING_UNEXPECTED_ERR_MSG);

            recyclerView.setVisibility(View.GONE);
            emptyDataTextView.setVisibility(View.VISIBLE);
            emptyDataReasonTextView.setVisibility(View.VISIBLE);

            return;
        }

        if (mCursorAdapter.getItemCount() > 0) {
            //Data present for recyclerView
            recyclerView.setVisibility(View.VISIBLE);
            emptyDataTextView.setVisibility(View.GONE);
            emptyDataReasonTextView.setVisibility(View.GONE);
        } else {
            //No data present for the recyclerView to show
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            @StockTaskService.StockServiceNetworkStatus int networkStatus = sharedPreferences.getInt(STRING_NETWORK_STATUS, -1);

            emptyDataTextView.setText(STRING_NO_DATA_PRESENT);
            switch (networkStatus) {
                case StockTaskService.STATUS_OK:
                    emptyDataReasonTextView.setText(STRING_NO_STOCKS_ADDED);
                    break;

                case StockTaskService.STATUS_NETWORK_UNAVAILABLE:
                    emptyDataReasonTextView.setText(STRING_NO_NETWORK);
                    break;

                case StockTaskService.STATUS_BAD_JSON:
                    emptyDataReasonTextView.setText(STRING_MALFORMED_RESPONSE);
                    break;

                case StockTaskService.STATUS_SERVER_DOWN:
                    emptyDataReasonTextView.setText(STRING_SERVER_DOWN);
                    break;

                case StockTaskService.STATUS_SERVER_ERROR:
                    emptyDataReasonTextView.setText(STRING_INTERNAL_SERVER_ERROR);
                    break;

                case StockTaskService.STATUS_BAD_REQUEST:
                    emptyDataReasonTextView.setText(STRING_UNEXPECTED_ERR_MSG);
                    break;

                case StockTaskService.STATUS_UNKNOWN:
                    emptyDataReasonTextView.setText(STRING_UNEXPECTED_ERR_MSG);
                    break;

                default:
                    emptyDataReasonTextView.setText(STRING_UNEXPECTED_ERR_MSG);
                    break;

            }
            recyclerView.setVisibility(View.GONE);
            emptyDataTextView.setVisibility(View.VISIBLE);
            emptyDataReasonTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);

        setEmptyViewUI(false);
    }

    public void networkToast() {
        Toast.makeText(mContext, STRING_NETWORK_TOAST, Toast.LENGTH_SHORT).show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_change_units) {
            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        mCursor = data;
        setEmptyViewUI(false);
        Utils.refreshWidget(mContext);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

}

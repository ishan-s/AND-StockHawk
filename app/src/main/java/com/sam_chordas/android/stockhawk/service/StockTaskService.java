package com.sam_chordas.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    private String LOG_TAG = StockTaskService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            STATUS_OK,
            STATUS_NETWORK_UNAVAILABLE,
            STATUS_SERVER_DOWN,
            STATUS_SERVER_ERROR,
            STATUS_BAD_JSON,
            STATUS_BAD_REQUEST,
            STATUS_UNKNOWN
    })
    public @interface StockServiceNetworkStatus {
    }

    public static final int STATUS_OK = 0;
    public static final int STATUS_NETWORK_UNAVAILABLE = 1;
    public static final int STATUS_SERVER_DOWN = 2;
    public static final int STATUS_SERVER_ERROR = 3;
    public static final int STATUS_BAD_JSON = 4;
    public static final int STATUS_BAD_REQUEST = 5;
    public static final int STATUS_UNKNOWN = 6;


    public StockTaskService() {
    }

    public StockTaskService(Context context) {
        mContext = context;
    }

    Response fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    static public void updateNetworkStatus(@StockServiceNetworkStatus int status, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(context.getString(R.string.network_status), status);
        editor.apply();
        editor.commit();
    }


    @Override
    public int onRunTask(TaskParams params) {
        Cursor initQueryCursor;
        if (mContext == null) {
            mContext = this;
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
                    + "in (", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            updateNetworkStatus(STATUS_BAD_REQUEST, mContext);
            e.printStackTrace();
            return GcmNetworkManager.RESULT_FAILURE;
        }
        if (params.getTag().equals("init") || params.getTag().equals("periodic")) {
            isUpdate = true;
            initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                    null, null);
            if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
                // Init task. Populates DB with quotes for the symbols seen below
                try {
                    urlStringBuilder.append(
                            URLEncoder.encode("\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    updateNetworkStatus(STATUS_BAD_REQUEST, mContext);
                    e.printStackTrace();
                    return GcmNetworkManager.RESULT_FAILURE;
                }
            } else if (initQueryCursor != null) {
                DatabaseUtils.dumpCursor(initQueryCursor);
                initQueryCursor.moveToFirst();
                for (int i = 0; i < initQueryCursor.getCount(); i++) {
                    mStoredSymbols.append("\"" +
                            initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")) + "\",");
                    initQueryCursor.moveToNext();
                }
                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                try {
                    urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    updateNetworkStatus(STATUS_BAD_REQUEST, mContext);
                    e.printStackTrace();
                    return GcmNetworkManager.RESULT_FAILURE;
                }
            }
        } else if (params.getTag().equals("add")) {
            isUpdate = false;
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString("symbol");
            try {
                urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                updateNetworkStatus(STATUS_BAD_REQUEST, mContext);
                e.printStackTrace();
                return GcmNetworkManager.RESULT_FAILURE;
            }
        }
        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        String urlString;
        String getResponse;
        int responseStatusCode;
        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null) {
            urlString = urlStringBuilder.toString();
            try {

                Response response = fetchData(urlString);
                responseStatusCode = response.code();
                switch (responseStatusCode) {
                    case 200:
                        updateNetworkStatus(STATUS_OK, mContext);
                        break;
                    case 500:
                        updateNetworkStatus(STATUS_SERVER_ERROR, mContext);
                        break;
                    case 503:
                        updateNetworkStatus(STATUS_SERVER_DOWN, mContext);
                        break;
                    case 404:
                        updateNetworkStatus(STATUS_SERVER_DOWN, mContext);
                        break;
                    case 400:
                        updateNetworkStatus(STATUS_BAD_REQUEST, mContext);
                        break;
                    default:
                        updateNetworkStatus(STATUS_UNKNOWN, mContext);
                        break;
                }

                getResponse = response.body().string();
                result = GcmNetworkManager.RESULT_SUCCESS;
                try {
                    ContentValues contentValues = new ContentValues();
                    // update ISCURRENT to 0 (false) so new data is current
                    if (isUpdate) {
                        contentValues.put(QuoteColumns.ISCURRENT, 0);
                        mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                null, null);
                    }

                    //validate the getResponse String to catch the case when the stock symbol is invalid
                    // will result in null values
                    ArrayList<ContentProviderOperation> batch = Utils.quoteJsonToContentVals(getResponse);
                    if (batch == null || batch.size() == 0) {
                        //Possibly non-existent stock was entered
                        // TODO: Figure out a way to show a meaningful error message - throw an intent to the main activity
                        mContext.sendBroadcast(new Intent("com.sam_chordas.android.stockhawk.ui.MyStocksActivity.INVALID_STOCK"));
                    } else {
                        mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, batch);
                    }

                } catch (RemoteException | OperationApplicationException e) {
                    updateNetworkStatus(STATUS_UNKNOWN, mContext);
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                }
            } catch (IOException e) {
                updateNetworkStatus(STATUS_SERVER_DOWN, mContext);
                e.printStackTrace();
            }
        }

        return result;
    }

}

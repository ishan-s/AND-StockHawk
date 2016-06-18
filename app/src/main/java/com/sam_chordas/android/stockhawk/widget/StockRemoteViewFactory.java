package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Const;

/**
 * Created by Ishan on 16-06-2016.
 */
public class StockRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private Cursor cursor;
    private int appWidId;
    private ContentResolver contentResolver;
    private String[] quoteQueryProjection = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CHANGE,
            QuoteColumns.ISUP
    };
    private int QUERY_INDEX_ID = 0;
    private int QUERY_INDEX_SYMBOL = 1;
    private int QUERY_INDEX_BIDPRICE = 2;
    private int QUERY_INDEX_CHANGE = 3;
    private int QUERY_INDEX_ISUP = 4;


    public StockRemoteViewFactory(Context context, Intent intent, ContentResolver contentResolver) {
        this.context = context;
        this.appWidId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        this.contentResolver = contentResolver;
    }

    public void setCursorForStocks() {
        if (contentResolver != null) {

            String whereClause = QuoteColumns.ISCURRENT + " = ?";
            String[] whereArgs = {"1"};

            cursor = contentResolver.query(QuoteProvider.Quotes.CONTENT_URI, quoteQueryProjection, whereClause, whereArgs, null);
        }
    }

    @Override
    public void onCreate() {
        setCursorForStocks();
    }

    @Override
    public void onDataSetChanged() {
        setCursorForStocks();
    }

    @Override
    public void onDestroy() {
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public int getCount() {
        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (cursor == null || context == null)
            return null;

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.list_item_quote);
        if (cursor.moveToPosition(position)) {
            String symbol = cursor.getString(QUERY_INDEX_SYMBOL);
            String bidPrice = cursor.getString(QUERY_INDEX_BIDPRICE);
            String change = cursor.getString(QUERY_INDEX_CHANGE);

            remoteViews.setTextViewText(R.id.stock_symbol, symbol);
            remoteViews.setTextViewText(R.id.bid_price, bidPrice);
            remoteViews.setTextViewText(R.id.change, change);

            Intent fillInIntent = new Intent();
            fillInIntent.setAction(StockHawkWidgetProvider.OPEN_STOCK);
            fillInIntent.putExtra(Const.EXTRA_STOCK_SYMBOL, symbol);
            remoteViews.setOnClickFillInIntent(R.id.list_item_quote_linearlayout, fillInIntent);

            if (cursor.getInt(QUERY_INDEX_ISUP) == 1)
                remoteViews.setInt(R.id.change, "setBackgroundResource",
                        R.drawable.percent_change_pill_green);
            else
                remoteViews.setInt(R.id.change, "setBackgroundResource",
                        R.drawable.percent_change_pill_red);
        }

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        //REMEMBER!!! Returning 0 instead of 1 would cause the remote views on the listview to not load
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (cursor != null)
            return cursor.getInt(QUERY_INDEX_ID);

        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}

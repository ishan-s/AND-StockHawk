package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Ishan on 16-06-2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StockHawkWidgetProvider extends AppWidgetProvider {
    private final String OPEN_STOCK = "OPEN_STOCK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidManager, int[] appWidInts){
        for(int widgetId : appWidInts){
            Intent intent = new Intent(context, StockHawkWidgetRemoteViewService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_stocks_main);
            remoteViews.setRemoteAdapter(R.id.widget_stocks_listview, intent);
            remoteViews.setEmptyView(R.id.widget_stocks_listview, R.id.widget_stocks_textview);

            Log.i("$$$STWP", "calling updateAppWidget");
            appWidManager.updateAppWidget(widgetId, remoteViews);
        }
        super.onUpdate(context, appWidManager, appWidInts);
    }

    @Override
    public void onReceive(Context context, Intent intent){
        String incomingIntentAction = intent.getAction();
        switch(incomingIntentAction){
            case OPEN_STOCK : break;
            default: //TODO
        }

        super.onReceive(context, intent);
    }
}

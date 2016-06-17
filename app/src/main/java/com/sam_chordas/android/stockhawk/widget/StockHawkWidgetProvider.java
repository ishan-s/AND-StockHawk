package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.StockGraphActivity;

/**
 * Created by Ishan on 16-06-2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StockHawkWidgetProvider extends AppWidgetProvider {
    public static final String OPEN_STOCK = "OPEN_STOCK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidManager, int[] appWidInts){
        for(int widgetId : appWidInts){
            Intent remoteViewServiceIntent = new Intent(context, StockHawkWidgetRemoteViewService.class);
            remoteViewServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            remoteViewServiceIntent.setData(Uri.parse(remoteViewServiceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_stocks_main);
            remoteViews.setRemoteAdapter(R.id.widget_stocks_listview, remoteViewServiceIntent);
            remoteViews.setEmptyView(R.id.widget_stocks_listview, R.id.widget_stocks_textview);

            Intent showGraphActivityIntent = new Intent(context, StockHawkWidgetProvider.class);
            showGraphActivityIntent.setAction(StockHawkWidgetProvider.OPEN_STOCK);
            showGraphActivityIntent.setData(Uri.parse(showGraphActivityIntent.toUri(Intent.URI_INTENT_SCHEME)));
            showGraphActivityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

            PendingIntent broadcastPendingIntent = PendingIntent.getBroadcast(context, 0, showGraphActivityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.widget_stocks_listview, broadcastPendingIntent);

            appWidManager.updateAppWidget(widgetId, remoteViews);
        }
        super.onUpdate(context, appWidManager, appWidInts);
    }

    @Override
    public void onReceive(Context context, Intent intent){
        String incomingIntentAction = intent.getAction();
        if (incomingIntentAction.equals(OPEN_STOCK)) {
            Intent startStockGraphActivityIntent = new Intent(context, StockGraphActivity.class);

            //REMEMBER: FLAG_ACTIVITY_NEW_TASK is needed for when starting an activity from outside.
            startStockGraphActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startStockGraphActivityIntent.putExtras(intent.getExtras());
            context.startActivity(startStockGraphActivityIntent);
        }

        super.onReceive(context, intent);
    }
}

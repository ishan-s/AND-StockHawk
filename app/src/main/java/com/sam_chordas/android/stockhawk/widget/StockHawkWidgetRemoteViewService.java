package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Ishan on 16-06-2016.
 */
public class StockHawkWidgetRemoteViewService extends RemoteViewsService{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewFactory(getApplicationContext(), intent, getContentResolver());
    }
}

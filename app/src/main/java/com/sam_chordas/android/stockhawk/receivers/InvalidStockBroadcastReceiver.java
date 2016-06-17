package com.sam_chordas.android.stockhawk.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by Ishan on 16-06-2016.
 */
public class InvalidStockBroadcastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent){
        Toast.makeText(context,
                R.string.invalid_stock_msg,
                Toast.LENGTH_SHORT).show();
    }
}

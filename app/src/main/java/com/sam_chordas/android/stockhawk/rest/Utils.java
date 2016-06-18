package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();
  public static int SNACKBAR_TYPE_INFO = 0;
  public static int SNACKBAR_TYPE_ERROR = 1;

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
          ContentProviderOperation cpo = buildBatchOperation(jsonObject);
          if(cpo!=null) batchOperations.add(cpo);
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              ContentProviderOperation cpo = buildBatchOperation(jsonObject);
              if(cpo!=null) batchOperations.add(cpo);
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  private static boolean isNullOrEmpty(JSONObject jsonObject, String key){
    try {
      String val = jsonObject.getString(key);
      Log.i("$$$", val);
      if(val==null || "null".equalsIgnoreCase(val) || "".equals(val.trim()) || val.length()==0)
        return true;
    }catch (JSONException je){
      Log.i("$DEBUG$", "JSONException while attempting to check isNullOrEmpty on "+key);
      return true;
    }
    return false;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    //Check the jsonObject for presence of nulls for critical attributes
    if(isNullOrEmpty(jsonObject, "Bid"))
      return null;

    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }

  public static String parseGraphDate(String inputDate){
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Date date = null;
    try {
      date = (Date) dateFormat.parse(inputDate);
    }catch(ParseException pe){
      pe.printStackTrace();
      return null;
    }

    DateFormat outputDateFormat = new SimpleDateFormat("dd MMM");
    return outputDateFormat.format(date);
  }

  public static void showSnackbar(View view, int messageResId, int snackbarType) {
    Snackbar snackbar = Snackbar.make(view, messageResId, Snackbar.LENGTH_LONG);
    snackbar.show();
  }

  public static boolean isNetworkConnected(Context context){
    ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    return (activeNetwork != null &&
            activeNetwork.isConnectedOrConnecting());

  }

}

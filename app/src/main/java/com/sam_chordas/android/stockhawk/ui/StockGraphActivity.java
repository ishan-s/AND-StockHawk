package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;


import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.graph.Date;
import com.sam_chordas.android.stockhawk.data.graph.Series;
import com.sam_chordas.android.stockhawk.data.graph.StockGraphData;
import com.sam_chordas.android.stockhawk.rest.Const;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockGraphService;


import java.util.ArrayList;
import java.util.List;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StockGraphActivity extends Activity {
    Retrofit retrofit;
    StockGraphService stockGraphService;
    LineChart lineChart;
    String symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_graph);

        Intent incomingIntent = getIntent();
        symbol = incomingIntent.getStringExtra("STOCK_SYMBOL");

        lineChart = (LineChart) findViewById(R.id.stock_linechart);

        Gson lenientGson = new GsonBuilder()
                .setLenient()
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl(Const.YAHOO_CHART_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        stockGraphService = retrofit.create(StockGraphService.class);
        Call<ResponseBody> stockGraphDataCall = stockGraphService.getGraphData("GOOG");

        stockGraphDataCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String responseStr = response.body().string();
                    String jsonFromResponse = responseStr.substring(responseStr.indexOf("(") + 1, responseStr.lastIndexOf(")"));

                    Gson gson = new GsonBuilder().create();
                    StockGraphData stockGraphData = gson.fromJson(jsonFromResponse, StockGraphData.class);
                    List<Series> seriesJson = stockGraphData.getSeries();
                    int xi=0;

                    ArrayList<Entry> entries = new ArrayList<>();
                    ArrayList<String> xvalues = new ArrayList<>();

                    String[] labels = new String[seriesJson.size()];
                    float[] values = new float[seriesJson.size()];

                    for(Series series : seriesJson){
                        String closeStr = String.valueOf(series.getClose());
                        String dateStr = String.valueOf(series.getDate());
                        float closeFloat = Float.parseFloat(closeStr);
                        Log.i("###", "date ="+dateStr+", close ="+closeStr+", closeFloat ="+closeFloat);

                        labels[xi] = dateStr;
                        values[xi] = closeFloat;

                        double yValue = closeFloat;
                        xvalues.add(Utils.convertDate(dateStr));
                        entries.add(new Entry((float) yValue, xi));
                        xi++;
                    }


                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setLabelsToSkip(30);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setTextSize(12f);
                    YAxis left = lineChart.getAxisLeft();
                    left.setEnabled(true);
                    left.setLabelCount(5, true);

                    lineChart.getAxisRight().setEnabled(false);

                    lineChart.getLegend().setTextSize(16f);

                    LineDataSet dataSet = new LineDataSet(entries, "GOOG");
                    LineData lineData = new LineData(xvalues, dataSet);
                    lineChart.setBackgroundColor(Color.WHITE);
                    lineChart.setData(lineData);
                    lineChart.invalidate();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("$D$", "Failure : "+t.toString());
            }
        });



    }
}

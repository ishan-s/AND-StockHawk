package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.graph.Date;
import com.sam_chordas.android.stockhawk.data.graph.Series;
import com.sam_chordas.android.stockhawk.data.graph.StockGraphData;
import com.sam_chordas.android.stockhawk.rest.Const;
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
    LineChartView lineChartView;
    LineSet lineSet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        lineChartView = (LineChartView) findViewById(R.id.stock_linechart);

        Gson lenientGson = new GsonBuilder()
                .setLenient()
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl(Const.YAHOO_CHART_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        stockGraphService = retrofit.create(StockGraphService.class);
        Call<ResponseBody> stockGraphDataCall = stockGraphService.getGraphData("GOOG");
        lineSet = new LineSet();
        lineChartView.addData(lineSet);


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
                    String[] labels = new String[seriesJson.size()];
                    float[] values = new float[seriesJson.size()];

                    for(Series series : seriesJson){
                        String closeStr = String.valueOf(series.getClose());
                        String dateStr = String.valueOf(series.getDate());
                        float closeFloat = Float.parseFloat(closeStr);
                        Log.i("###", "date ="+dateStr+", close ="+closeStr+", closeFloat ="+closeFloat);

                        labels[xi] = dateStr;
                        values[xi] = closeFloat;

                        lineSet.addPoint("", closeFloat);
                        xi++;
                    }

                    lineChartView.notifyDataUpdate();
                    lineChartView.show();
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

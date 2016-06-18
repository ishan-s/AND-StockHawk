package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.graph.Series;
import com.sam_chordas.android.stockhawk.data.graph.StockGraphData;
import com.sam_chordas.android.stockhawk.rest.Const;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockGraphService;


import java.util.ArrayList;
import java.util.List;


import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StockGraphActivity extends AppCompatActivity {
    Retrofit retrofit;
    StockGraphService stockGraphService;

    @BindView(R.id.stock_graph_linechart) LineChart stockLineChart;

    @BindColor(R.color.material_blue_500) int COLOR_MATERIAL_BLUE_500;
    @BindColor(R.color.material_dark_blue_900) int COLOR_MATERIAL_DARK_BLUE_900;
    @BindString(R.string.graph_description_text) String STRING_GRAPH_DESCRIPTION_TEXT;
    @BindString(R.string.stock_graph_activity_title) String STRING_GRAPH_ACTIVITY_TITLE;
    @BindString(R.string.graph_no_data_text) String STRING_GRAPH_NO_DATA_TEXT;

    String symbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        ButterKnife.bind(this);

        Intent incomingIntent = getIntent();
        symbol = incomingIntent.getStringExtra(Const.EXTRA_STOCK_SYMBOL);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(STRING_GRAPH_ACTIVITY_TITLE+symbol);

        retrofit = new Retrofit.Builder()
                .baseUrl(Const.YAHOO_CHART_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        stockGraphService = retrofit.create(StockGraphService.class);
        Call<ResponseBody> stockGraphDataCall = stockGraphService.getGraphData(symbol);

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

                    ArrayList<Entry> closingQuotesYAxis = new ArrayList<>();
                    ArrayList<String> datesXAxis = new ArrayList<>();

                    for(Series series : seriesJson){
                        double yValue = (double) series.getClose();
                        datesXAxis.add(Utils.parseGraphDate(String.valueOf(series.getDate())));
                        closingQuotesYAxis.add(new Entry((float) yValue, xi++));
                    }

                    updateLineChart(datesXAxis, closingQuotesYAxis);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                stockLineChart.setNoDataText(STRING_GRAPH_NO_DATA_TEXT);
            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateLineChart(ArrayList<String> xAxisValues, ArrayList<Entry> yAxisValues){
        XAxis xAxis = stockLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(8f);
        xAxis.enableGridDashedLine(10.0f, 3.0f, 1.0f);

        YAxis yAxis = stockLineChart.getAxisLeft();
        yAxis.setEnabled(true);
        yAxis.setLabelCount(10, true);
        yAxis.enableGridDashedLine(10.0f, 3.0f, 1.0f);

        stockLineChart.getAxisRight().setEnabled(false);
        stockLineChart.getLegend().setTextSize(14f);
        stockLineChart.setDescription(STRING_GRAPH_DESCRIPTION_TEXT);

        LineDataSet dataSet = new LineDataSet(yAxisValues, symbol);
        dataSet.setCircleColor(COLOR_MATERIAL_DARK_BLUE_900);
        dataSet.setCircleColorHole(COLOR_MATERIAL_DARK_BLUE_900);
        dataSet.setColor(COLOR_MATERIAL_BLUE_500);
        dataSet.setLineWidth(3);
        dataSet.setCircleRadius(3.0f);

        LineData lineData = new LineData(xAxisValues, dataSet);
        stockLineChart.setBackgroundColor(Color.WHITE);
        stockLineChart.setData(lineData);
        stockLineChart.invalidate();

    }
}

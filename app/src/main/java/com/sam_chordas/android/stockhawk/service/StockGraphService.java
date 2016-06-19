package com.sam_chordas.android.stockhawk.service;

import com.sam_chordas.android.stockhawk.data.graph.StockGraphData;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/** Retrofit service class for invoking the Yahoo! Chart API
 * Created by Ishan on 17-06-2016.
 */
public interface StockGraphService {

/*    @GET("{symbol}/chartdata;type=quote;range=1y/json")
    Call<StockGraphData> getGraphData(
        @Path("symbol") String symbol
    );
*/
    @GET("{symbol}/chartdata;type=quote;range=3m/json")
    Call<ResponseBody> getGraphData(
            @Path("symbol") String symbol
    );
}

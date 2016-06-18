
package com.sam_chordas.android.stockhawk.data.graph;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class StockGraphData  {

    @SerializedName("meta")
    @Expose
    private Meta meta;
    @SerializedName("Date")
    @Expose
    private Date date;
    @SerializedName("labels")
    @Expose
    private List<Integer> labels = new ArrayList<Integer>();
    @SerializedName("ranges")
    @Expose
    private Ranges ranges;
    @SerializedName("series")
    @Expose
    private List<Series> series = new ArrayList<Series>();

    /**
     * 
     * @return
     *     The meta
     */
    public Meta getMeta() {
        return meta;
    }

    /**
     * 
     * @param meta
     *     The meta
     */
    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    /**
     * 
     * @return
     *     The date
     */
    public Date getDate() {
        return date;
    }

    /**
     * 
     * @param date
     *     The Date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * 
     * @return
     *     The labels
     */
    public List<Integer> getLabels() {
        return labels;
    }

    /**
     * 
     * @param labels
     *     The labels
     */
    public void setLabels(List<Integer> labels) {
        this.labels = labels;
    }

    /**
     * 
     * @return
     *     The ranges
     */
    public Ranges getRanges() {
        return ranges;
    }

    /**
     * 
     * @param ranges
     *     The ranges
     */
    public void setRanges(Ranges ranges) {
        this.ranges = ranges;
    }

    /**
     * 
     * @return
     *     The series
     */
    public List<Series> getSeries() {
        return series;
    }

    /**
     * 
     * @param series
     *     The series
     */
    public void setSeries(List<Series> series) {
        this.series = series;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

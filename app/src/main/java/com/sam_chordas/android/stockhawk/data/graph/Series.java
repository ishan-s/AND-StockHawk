
package com.sam_chordas.android.stockhawk.data.graph;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class Series {

    @SerializedName("Date")
    @Expose
    private Integer date;
    @SerializedName("close")
    @Expose
    private Double close;
    @SerializedName("high")
    @Expose
    private Double high;
    @SerializedName("low")
    @Expose
    private Double low;
    @SerializedName("open")
    @Expose
    private Double open;
    @SerializedName("volume")
    @Expose
    private Integer volume;

    /**
     * 
     * @return
     *     The date
     */
    public Integer getDate() {
        return date;
    }

    /**
     * 
     * @param date
     *     The Date
     */
    public void setDate(Integer date) {
        this.date = date;
    }

    /**
     * 
     * @return
     *     The close
     */
    public Double getClose() {
        return close;
    }

    /**
     * 
     * @param close
     *     The close
     */
    public void setClose(Double close) {
        this.close = close;
    }

    /**
     * 
     * @return
     *     The high
     */
    public Double getHigh() {
        return high;
    }

    /**
     * 
     * @param high
     *     The high
     */
    public void setHigh(Double high) {
        this.high = high;
    }

    /**
     * 
     * @return
     *     The low
     */
    public Double getLow() {
        return low;
    }

    /**
     * 
     * @param low
     *     The low
     */
    public void setLow(Double low) {
        this.low = low;
    }

    /**
     * 
     * @return
     *     The open
     */
    public Double getOpen() {
        return open;
    }

    /**
     * 
     * @param open
     *     The open
     */
    public void setOpen(Double open) {
        this.open = open;
    }

    /**
     * 
     * @return
     *     The volume
     */
    public Integer getVolume() {
        return volume;
    }

    /**
     * 
     * @param volume
     *     The volume
     */
    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

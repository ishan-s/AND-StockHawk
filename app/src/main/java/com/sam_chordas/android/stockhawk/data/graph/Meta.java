
package com.sam_chordas.android.stockhawk.data.graph;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class Meta {

    @SerializedName("uri")
    @Expose
    private String uri;
    @SerializedName("ticker")
    @Expose
    private String ticker;
    @SerializedName("Company-Name")
    @Expose
    private String companyName;
    @SerializedName("Exchange-Name")
    @Expose
    private String exchangeName;
    @SerializedName("unit")
    @Expose
    private String unit;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("first-trade")
    @Expose
    private String firstTrade;
    @SerializedName("last-trade")
    @Expose
    private String lastTrade;
    @SerializedName("currency")
    @Expose
    private String currency;
    @SerializedName("previous_close_price")
    @Expose
    private Double previousClosePrice;

    /**
     * 
     * @return
     *     The uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * 
     * @param uri
     *     The uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * 
     * @return
     *     The ticker
     */
    public String getTicker() {
        return ticker;
    }

    /**
     * 
     * @param ticker
     *     The ticker
     */
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    /**
     * 
     * @return
     *     The companyName
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * 
     * @param companyName
     *     The Company-Name
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * 
     * @return
     *     The exchangeName
     */
    public String getExchangeName() {
        return exchangeName;
    }

    /**
     * 
     * @param exchangeName
     *     The Exchange-Name
     */
    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    /**
     * 
     * @return
     *     The unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * 
     * @param unit
     *     The unit
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * 
     * @return
     *     The timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * 
     * @param timestamp
     *     The timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 
     * @return
     *     The firstTrade
     */
    public String getFirstTrade() {
        return firstTrade;
    }

    /**
     * 
     * @param firstTrade
     *     The first-trade
     */
    public void setFirstTrade(String firstTrade) {
        this.firstTrade = firstTrade;
    }

    /**
     * 
     * @return
     *     The lastTrade
     */
    public String getLastTrade() {
        return lastTrade;
    }

    /**
     * 
     * @param lastTrade
     *     The last-trade
     */
    public void setLastTrade(String lastTrade) {
        this.lastTrade = lastTrade;
    }

    /**
     * 
     * @return
     *     The currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * 
     * @param currency
     *     The currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * 
     * @return
     *     The previousClosePrice
     */
    public Double getPreviousClosePrice() {
        return previousClosePrice;
    }

    /**
     * 
     * @param previousClosePrice
     *     The previous_close_price
     */
    public void setPreviousClosePrice(Double previousClosePrice) {
        this.previousClosePrice = previousClosePrice;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}

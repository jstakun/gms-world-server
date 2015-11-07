package com.jstakun.lm.server.persistence;

import java.util.Date;
import java.util.List;

/**
 *
 * @author jstakun
 */
public class Hotel {
	private int hotelId;
    private String hotelFileName;
    private String hotelName;
    private double rating;
    private int cityId;
    private String cityFileName;
    private String cityName;
    private int stateId;
    private String stateFileName;
    private String stateName;
    private String countryCode;
    private String countryFileName;
    private String countryName;
    private int imageId;
    private String Address;
    private double minRate;
    private String currencyCode;
    private double Latitude;
    private double Longitude;
    private int NumberOfReviews;
    private double ConsumerRating;
    private String PropertyType;
    private int ChainID;
    private List<String> GeoCells;
    private Date lastUpdateDate;

    /**
     * @return the hotelId
     */
    public int getHotelId() {
        return hotelId;
    }

    /**
     * @param hotelId the hotelId to set
     */
    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    /**
     * @return the hotelFileName
     */
    public String getHotelFileName() {
        return hotelFileName;
    }

    /**
     * @param hotelFileName the hotelFileName to set
     */
    public void setHotelFileName(String hotelFileName) {
        this.hotelFileName = hotelFileName;
    }

    /**
     * @return the hotelName
     */
    public String getHotelName() {
        return hotelName;
    }

    /**
     * @param hotelName the hotelName to set
     */
    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    /**
     * @return the rating
     */
    public double getRating() {
        return rating;
    }

    /**
     * @param rating the rating to set
     */
    public void setRating(double rating) {
        this.rating = rating;
    }

    /**
     * @return the cityId
     */
    public int getCityId() {
        return cityId;
    }

    /**
     * @param cityId the cityId to set
     */
    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    /**
     * @return the cityFileName
     */
    public String getCityFileName() {
        return cityFileName;
    }

    /**
     * @param cityFileName the cityFileName to set
     */
    public void setCityFileName(String cityFileName) {
        this.cityFileName = cityFileName;
    }

    /**
     * @return the cityName
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * @param cityName the cityName to set
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * @return the stateId
     */
    public int getStateId() {
        return stateId;
    }

    /**
     * @param stateId the stateId to set
     */
    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    /**
     * @return the stateFileName
     */
    public String getStateFileName() {
        return stateFileName;
    }

    /**
     * @param stateFileName the stateFileName to set
     */
    public void setStateFileName(String stateFileName) {
        this.stateFileName = stateFileName;
    }

    /**
     * @return the stateName
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * @param stateName the stateName to set
     */
    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    /**
     * @return the countryCode
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * @param countryCode the countryCode to set
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * @return the countryFileName
     */
    public String getCountryFileName() {
        return countryFileName;
    }

    /**
     * @param countryFileName the countryFileName to set
     */
    public void setCountryFileName(String countryFileName) {
        this.countryFileName = countryFileName;
    }

    /**
     * @return the countryName
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * @param countryName the countryName to set
     */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    /**
     * @return the imageId
     */
    public int getImageId() {
        return imageId;
    }

    /**
     * @param imageId the imageId to set
     */
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    /**
     * @return the Address
     */
    public String getAddress() {
        return Address;
    }

    /**
     * @param Address the Address to set
     */
    public void setAddress(String Address) {
        this.Address = Address;
    }

    /**
     * @return the minRate
     */
    public double getMinRate() {
        return minRate;
    }

    /**
     * @param minRate the minRate to set
     */
    public void setMinRate(double minRate) {
        this.minRate = minRate;
    }

    /**
     * @return the currencyCode
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * @param currencyCode the currencyCode to set
     */
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    /**
     * @return the Latitude
     */
    public double getLatitude() {
        return Latitude;
    }

    /**
     * @param Latitude the Latitude to set
     */
    public void setLatitude(double Latitude) {
        this.Latitude = Latitude;
    }

    /**
     * @return the Longitude
     */
    public double getLongitude() {
        return Longitude;
    }

    /**
     * @param Longitude the Longitude to set
     */
    public void setLongitude(double Longitude) {
        this.Longitude = Longitude;
    }

    /**
     * @return the NumberOfReviews
     */
    public int getNumberOfReviews() {
        return NumberOfReviews;
    }

    /**
     * @param NumberOfReviews the NumberOfReviews to set
     */
    public void setNumberOfReviews(int NumberOfReviews) {
        this.NumberOfReviews = NumberOfReviews;
    }

    /**
     * @return the ConsumerRating
     */
    public double getConsumerRating() {
        return ConsumerRating;
    }

    /**
     * @param ConsumerRating the ConsumerRating to set
     */
    public void setConsumerRating(double ConsumerRating) {
        this.ConsumerRating = ConsumerRating;
    }

    /**
     * @return the PropertyType
     */
    public String getPropertyType() {
        return PropertyType;
    }

    /**
     * @param PropertyType the PropertyType to set
     */
    public void setPropertyType(String PropertyType) {
        this.PropertyType = PropertyType;
    }

    /**
     * @return the ChainID
     */
    public int getChainID() {
        return ChainID;
    }

    /**
     * @param ChainID the ChainID to set
     */
    public void setChainID(int ChainID) {
        this.ChainID = ChainID;
    }

    /**
     * @return the Facilities
     */
    public List<String> getGeoCells() {
        return GeoCells;
    }

    /**
     * @param GeoCells the GeoCells to set
     */
    public void setGeoCells(List<String> GeoCells) {
        this.GeoCells = GeoCells;
    }

    /**
     * @return the lastUpdateDate
     */
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    /**
     * @param lastUpdateDate the lastUpdateDate to set
     */
    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}

package org.openmrs.mobile.models;

public class AddressDto {
    private boolean preferred;
    private String address1;
    private String address2;
    private String cityVillage;
    private String stateProvince;
    private String country;
    private String postalCode;
    private String countyDistrict;
    private String address3;
    private String address4;
    private String address5;
    private String address6;
    private String startDate;
    private String endDate;
    private String latitude;
    private String longitude;

    // Constructor with all fields
    public AddressDto(boolean preferred, String address1, String address2, String cityVillage,
                   String stateProvince, String country, String postalCode, String countyDistrict,
                   String address3, String address4, String address5, String address6,
                   String startDate, String endDate, String latitude, String longitude) {
        this.preferred = preferred;
        this.address1 = address1;
        this.address2 = address2;
        this.cityVillage = cityVillage;
        this.stateProvince = stateProvince;
        this.country = country;
        this.postalCode = postalCode;
        this.countyDistrict = countyDistrict;
        this.address3 = address3;
        this.address4 = address4;
        this.address5 = address5;
        this.address6 = address6;
        this.startDate = startDate;
        this.endDate = endDate;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public AddressDto(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isPreferred() {
        return preferred;
    }

    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCityVillage() {
        return cityVillage;
    }

    public void setCityVillage(String cityVillage) {
        this.cityVillage = cityVillage;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountyDistrict() {
        return countyDistrict;
    }

    public void setCountyDistrict(String countyDistrict) {
        this.countyDistrict = countyDistrict;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getAddress4() {
        return address4;
    }

    public void setAddress4(String address4) {
        this.address4 = address4;
    }

    public String getAddress5() {
        return address5;
    }

    public void setAddress5(String address5) {
        this.address5 = address5;
    }

    public String getAddress6() {
        return address6;
    }

    public void setAddress6(String address6) {
        this.address6 = address6;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}


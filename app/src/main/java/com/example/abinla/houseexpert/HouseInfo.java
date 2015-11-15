package com.example.abinla.houseexpert;

/**
 * Created by abinla on 2015/10/31.
 */
public class HouseInfo {
    private String address;
    private String price;
    private String structure;
    private String saledate;
    private String latlng;
    private String sub;

    HouseInfo(String _address, String _price, String _sub, String _structure, String _saledate, String _latlng) {
        address = _address;
        price = _price;
        structure =_structure;
        saledate = _saledate;
        latlng = _latlng;
        sub = _sub;

    }
    HouseInfo(String _address, String _price) {
        address = _address;
        price = _price;
        structure ="";
        saledate = "";
        latlng = "";
        sub = "";
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    HouseInfo() {
        address = "";
        price = "";
        structure ="";

        saledate = "";
        latlng = "";
        sub = "";
    }
    public String getAddress() {
        return address;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLatlng() {
        return latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public String getSaledate() {
        return saledate;
    }

    public void setSaledate(String saledate) {
        this.saledate = saledate;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String toString(){
       return   address + " "+ sub +" " + price +" " + structure + " " + saledate + " " + latlng;
    }
}
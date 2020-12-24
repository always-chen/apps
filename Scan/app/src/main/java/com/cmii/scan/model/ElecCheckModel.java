package com.cmii.scan.model;

public class ElecCheckModel {
    private String code; // 发票代码
    private String num; // 发票号码
    private String date; // 发票日期
    private String price; // 发票金额
    private String validCode; // 校验码
    private String lastSixValidCode; // 后六位校验码

    public ElecCheckModel(String code, String num, String date, String price, String validCode, String lastSixValidCode) {
        this.code = code;
        this.num = num;
        this.date = date;
        this.price = price;
        this.validCode = validCode;
        this.lastSixValidCode = lastSixValidCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getValidCode() {
        return validCode;
    }

    public void setValidCode(String validCode) {
        this.validCode = validCode;
    }

    public String getLastSixValidCode() {
        return lastSixValidCode;
    }

    public void setLastSixValidCode(String lastSixValidCode) {
        this.lastSixValidCode = lastSixValidCode;
    }

    @Override
    public String toString() {
        return "ElecCheckModel{" +
                "code='" + code + '\'' +
                ", num='" + num + '\'' +
                ", date='" + date + '\'' +
                ", price='" + price + '\'' +
                ", validCode='" + validCode + '\'' +
                ", lastSixValidCode='" + lastSixValidCode + '\'' +
                '}';
    }
}

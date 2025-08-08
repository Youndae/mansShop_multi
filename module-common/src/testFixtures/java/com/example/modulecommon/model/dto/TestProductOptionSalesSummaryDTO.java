package com.example.modulecommon.model.dto;

public class TestProductOptionSalesSummaryDTO {

    private String size;

    private String color;

    private long sales;

    private long salesQuantity;

    public TestProductOptionSalesSummaryDTO() {
    }

    public TestProductOptionSalesSummaryDTO(String size, String color, long sales, long salesQuantity) {
        this.size = size;
        this.color = color;
        this.sales = sales;
        this.salesQuantity = salesQuantity;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public long getSales() {
        return sales;
    }

    public long getSalesQuantity() {
        return salesQuantity;
    }

    public void addSalesFieldsValue(long sales, long salesQuantity) {
        this.sales += sales;
        this.salesQuantity += salesQuantity;
    }
}
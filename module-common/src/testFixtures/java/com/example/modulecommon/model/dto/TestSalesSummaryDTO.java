package com.example.modulecommon.model.dto;

public class TestSalesSummaryDTO {

    private long sales;

    private long salesQuantity;

    private long orderQuantity;

    public TestSalesSummaryDTO() {
        this.sales = 0L;
        this.salesQuantity = 0L;
        this.orderQuantity = 0L;
    }

    public void addFieldsValue(long sales, long salesQuantity, long orderQuantity) {
        this.sales += sales;
        this.salesQuantity += salesQuantity;
        this.orderQuantity += orderQuantity;
    }

    public long getSales() {
        return sales;
    }

    public long getSalesQuantity() {
        return salesQuantity;
    }

    public long getOrderQuantity() {
        return orderQuantity;
    }

    public void setSalesQuantity(long salesQuantity) {
        this.salesQuantity = salesQuantity;
    }

    @Override
    public String toString() {
        return "TestSalesSummaryDTO{" +
                "sales=" + sales +
                ", salesQuantity=" + salesQuantity +
                ", orderQuantity=" + orderQuantity +
                '}';
    }
}

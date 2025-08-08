package com.example.modulecommon.model.dto;

public class TestProductSalesSummaryDTO {

    private String classificationId;

    private String productName;

    private long sales;

    private long salesQuantity;

    public TestProductSalesSummaryDTO() {
    }

    public TestProductSalesSummaryDTO(String classificationId, String productName, long sales, long salesQuantity) {
        this.classificationId = classificationId;
        this.productName = productName;
        this.sales = sales;
        this.salesQuantity = salesQuantity;
    }

    public String getClassificationId() {
        return classificationId;
    }

    public String getProductName() {
        return productName;
    }

    public long getSales() {
        return sales;
    }

    public long getSalesQuantity() {
        return salesQuantity;
    }

    public void addSalesValue(long sales) {
        this.sales += sales;
    }
}

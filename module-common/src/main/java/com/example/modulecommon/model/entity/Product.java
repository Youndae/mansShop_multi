package com.example.modulecommon.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "classificationId")
    private Classification classification;

    private String productName;

    private int productPrice;

    private String thumbnail;

    private boolean isOpen;

    private Long productSales;

    private int productDiscount;

    @CreationTimestamp
    private LocalDate createdAt;

    @UpdateTimestamp
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private final List<ProductOption> productOptionSet = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private final Set<ProductThumbnail> productThumbnailSet = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private final Set<ProductInfoImage> productInfoImageSet = new HashSet<>();

    public void addProductOption(ProductOption productOption) {
        productOptionSet.add(productOption);
        productOption.setProduct(this);
    }

    public void addProductThumbnail(ProductThumbnail productThumbnail) {
        productThumbnailSet.add(productThumbnail);
        productThumbnail.setProduct(this);
    }

    public void addProductInfoImage(ProductInfoImage productInfoImage) {
        productInfoImageSet.add(productInfoImage);
        productInfoImage.setProduct(this);
    }

    public void setProductSales(long productSales) {
        this.productSales = productSales;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setId(String productId){
        this.id = productId;
    }
}

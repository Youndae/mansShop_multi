package com.example.modulecommon.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
public class Product {

    @Id
    @Column(length = 200)
    private String id;

    @ManyToOne
    @JoinColumn(name = "classificationId", nullable = false)
    private Classification classification;

    @Column(length = 200,
            nullable = false
    )
    private String productName;

    @Column(nullable = false)
    private int productPrice;

    @Column(length = 255,
            nullable = false
    )
    private String thumbnail;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 1",
            nullable = false
    )
    private boolean isOpen;

    @Column(columnDefinition = "BIGINT DEFAULT 0",
            nullable = false
    )
    private Long productSalesQuantity;

    @Column(columnDefinition = "INT DEFAULT 0",
            nullable = false
    )
    private int productDiscount;

    @CreationTimestamp
    @Column(nullable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product")
    private final List<ProductOption> productOptions = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private final List<ProductThumbnail> productThumbnails = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private final List<ProductInfoImage> productInfoImages = new ArrayList<>();

    public void addProductOption(ProductOption option) {
        productOptions.add(option);
        option.setProduct(this);
    }

    public void addProductThumbnail(ProductThumbnail thumbnail) {
        productThumbnails.add(thumbnail);
        thumbnail.setProduct(this);
    }

    public void addProductInfoImage(ProductInfoImage infoImage) {
        productInfoImages.add(infoImage);
        infoImage.setProduct(this);
    }

    public void setProductSalesQuantity(long productSalesQuantity) {
        this.productSalesQuantity = productSalesQuantity;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setId(String productId){
        this.id = productId;
    }

    /*public void setPatchData(AdminProductPatchDTO patchDTO, Classification classification) {
        this.productName = patchDTO.getProductName();
        this.classification = classification;
        this.productPrice = patchDTO.getPrice();
        this.isOpen = patchDTO.getIsOpen();
        this.productDiscount = patchDTO.getDiscount();
    }*/

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", classification=" + classification +
                ", productName='" + productName + '\'' +
                ", productPrice=" + productPrice +
                ", thumbnail='" + thumbnail + '\'' +
                ", isOpen=" + isOpen +
                ", productSalesQuantity=" + productSalesQuantity +
                ", productDiscount=" + productDiscount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", productOptions=" + productOptions +
                '}';
    }
}

package com.example.modulecommon.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "productOption")
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private Product product;

    @Column(length = 20)
    private String size;

    @Column(length = 50)
    private String color;

    @Column(columnDefinition = "INT DEFAULT 0",
            nullable = false
    )
    private int stock;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 1",
            nullable = false
    )
    private boolean isOpen;

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    /*public void patchOptionData(PatchOptionDTO dto) {
        this.size = dto.getSize();
        this.color = dto.getColor();
        this.stock = dto.getOptionStock();
        this.isOpen = dto.isOptionIsOpen();
    }*/

    @Override
    public String toString() {
        return "ProductOption{" +
                "id=" + id +
                ", size='" + size + '\'' +
                ", color='" + color + '\'' +
                ", stock=" + stock +
                ", isOpen=" + isOpen +
                '}';
    }
}

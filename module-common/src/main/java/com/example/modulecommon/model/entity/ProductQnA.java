package com.example.modulecommon.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductQnA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "productId")
    private Product product;

    private String qnaContent;

    @OneToMany(mappedBy = "productQnA", cascade = CascadeType.ALL)
    private final List<ProductQnAReply> productQnAReplies = new ArrayList<>();

    @CreationTimestamp
    private LocalDate createdAt;

    private boolean productQnAStat;

    public void addProductQnAReply(ProductQnAReply reply) {
        productQnAReplies.add(reply);
        reply.setProductQnA(this);
    }

    public void setProductQnAStat(boolean productQnAStat) {
        this.productQnAStat = productQnAStat;
    }
}

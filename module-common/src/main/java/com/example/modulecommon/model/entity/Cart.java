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
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private Member member;

    @Column(length = 255)
    private String cookieId;

    @CreationTimestamp
    @Column(nullable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cart", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private final List<CartDetail> cartDetailList = new ArrayList<>();

    public void addCartDetail(CartDetail cartDetail) {
        cartDetailList.add(cartDetail);
        cartDetail.setCart(this);
    }

}

package com.example.modulecommon.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private Member member;

    private String cookieId;

    @CreationTimestamp
    private LocalDate createdAt;

    @UpdateTimestamp
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "cart"
    , cascade = CascadeType.ALL)
    private final Set<CartDetail> cartDetailSet = new HashSet<>();

    public void addCartDetail(CartDetail cartDetail) {
        cartDetailSet.add(cartDetail);
        cartDetail.setCart(this);
    }

}

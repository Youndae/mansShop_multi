package com.example.modulecommon.model.entity;

import com.example.modulecommon.model.enumuration.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "auth")
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private Member member;

    @Column(length = 50,
            nullable = false
    )
    private String auth;

    public void setMember(Member member) {
        this.member = member;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public Auth toMemberAuth() {
        return Auth.builder()
                .auth(Role.MEMBER.getKey())
                .build();
    }
}

package com.example.modulecommon.model.entity;

import com.example.modulecommon.model.dto.oAuth.OAuth2DTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Member {

    @Id
    private String userId;

    private String userPw;

    private String userName;

    private String nickname;

    private String userEmail;

    private String provider;

    private Long memberPoint;

    @CreationTimestamp
    private LocalDate createdAt;

    private String phone;

    private LocalDate birth;

    @OneToMany(mappedBy = "member",
    fetch = FetchType.EAGER,
    cascade = CascadeType.ALL)
    private final List<Auth> auths = new ArrayList<>();

    public void addMemberAuth(Auth auth) {
        auths.add(auth);
        auth.setMember(this);
    }

    public void setUserPw(String userPw) {
        this.userPw = userPw;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setMemberPoint(Long memberPoint) {
        this.memberPoint = memberPoint;
    }

    public OAuth2DTO toOAuth2DTOUseFilter() {
        return new OAuth2DTO(
                this.userId,
                this.userName,
                this.auths,
                null
        );
    }
}

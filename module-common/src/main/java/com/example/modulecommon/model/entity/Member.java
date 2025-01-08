package com.example.modulecommon.model.entity;

import com.example.modulecommon.model.dto.oAuth.OAuth2DTO;
import com.example.modulecommon.model.enumuration.OAuthProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    @Builder
    public Member(String userId,
                  String userPw,
                  String userName,
                  String nickname,
                  String userEmail,
                  String provider,
                  Long memberPoint,
                  String phone,
                  LocalDate birth) {
        String phoneRegEx = "(\\d{3})(\\d{3,4})(\\d{4})";

        this.userId = userId;
        this.userPw = userPw == null ? null : encodePw(userPw);
        this.userName = userName;
        this.nickname = nickname;
        this.userEmail = userEmail;
        this.provider = provider == null ? OAuthProvider.LOCAL.getKey() : provider;
        this.memberPoint = memberPoint == null ? 0: memberPoint;
        this.phone = phone == null ? null : phone.replaceAll(phoneRegEx, "$1-$2-$3");
        this.birth = birth;
    }

    private String encodePw(String userPw) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        return passwordEncoder.encode(userPw);
    }

    public void setUserPw(String userPw) {
        this.userPw = encodePw(userPw);
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

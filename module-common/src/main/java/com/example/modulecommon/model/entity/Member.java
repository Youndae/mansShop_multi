package com.example.modulecommon.model.entity;

import com.example.modulecommon.model.dto.oAuth.OAuth2DTO;
import com.example.modulecommon.model.enumuration.OAuthProvider;
import com.example.modulecommon.utils.PhoneNumberUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Table(name = "member")
public class Member {

    @Id
    @Column(length = 50)
    private String userId;

    @Column(length = 200)
    private String userPw;

    @Column(length = 100,
            nullable = false
    )
    private String userName;

    @Column(length = 100)
    private String nickname;

    @Column(length = 100,
            nullable = false
    )
    private String userEmail;

    @Column(length = 20,
           nullable = false
    )
    private String provider;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Long memberPoint;

    @CreationTimestamp
    @Column(nullable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDate createdAt;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime updatedAt;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "DATE")
    private LocalDate birth;

    @OneToMany(mappedBy = "member")
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
        this.userId = userId;
        this.userPw = userPw == null ? null : encodePw(userPw);
        this.userName = userName;
        this.nickname = nickname;
        this.userEmail = userEmail;
        this.provider = provider == null ? "local" : provider;
        this.memberPoint = memberPoint == null ? 0 : memberPoint;
        this.phone = PhoneNumberUtils.format(phone);
        this.birth = birth;
    }

    private String encodePw(String userPw) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        return passwordEncoder.encode(userPw);
    }

    public void setUserPw(String userPw) {
        this.userPw = encodePw(userPw);
    }

    public void setMemberPoint(Long memberPoint) {
        this.memberPoint = memberPoint;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void patchUser(String nickname, String phone, String userEmail) {

        this.nickname = nickname;
        this.phone = PhoneNumberUtils.format(phone);
        this.userEmail = userEmail;
    }

    public OAuth2DTO toOAuth2DTOUseFilter() {
        return new OAuth2DTO(
                this.userId
                , this.userName
                , this.auths
                , null
        );
    }
}

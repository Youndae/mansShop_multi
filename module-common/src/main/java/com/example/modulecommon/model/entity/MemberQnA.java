package com.example.modulecommon.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberQnA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "qnaClassificationId")
    private QnAClassification qnAClassification;

    private String memberQnATitle;

    private String memberQnAContent;

    @CreationTimestamp
    private LocalDate createdAt;

    @UpdateTimestamp
    private LocalDate updatedAt;

    private boolean memberQnAStat;

    public void setModifyData (String title, String content, QnAClassification qnaClassification) {
        this.qnAClassification = qnaClassification;
        this.memberQnATitle = title;
        this.memberQnAContent = content;
    }

    public void setMemberQnAStat(boolean memberQnAStat) {
        this.memberQnAStat = memberQnAStat;
    }
}

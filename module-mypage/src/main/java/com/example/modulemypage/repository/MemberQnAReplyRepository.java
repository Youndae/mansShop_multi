package com.example.modulemypage.repository;

import com.example.modulecommon.model.entity.MemberQnAReply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberQnAReplyRepository extends JpaRepository<MemberQnAReply,Long>, MemberQnAReplyDSLRepository{
}

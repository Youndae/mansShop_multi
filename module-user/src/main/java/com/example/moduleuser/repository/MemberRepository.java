package com.example.moduleuser.repository;

import com.example.modulecommon.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String>, MemberDSLRepository {

    Member findByNickname(String nickname);
}

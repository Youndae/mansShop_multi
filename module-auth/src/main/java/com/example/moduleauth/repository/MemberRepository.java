package com.example.moduleauth.repository;

import com.example.modulecommon.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String>, MemberDSLRepository {
}

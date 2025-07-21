package com.example.modulemypage.repository;

import com.example.modulecommon.model.entity.MemberQnA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberQnARepository extends JpaRepository<MemberQnA,Long>, MemberQnADSLRepository{
}

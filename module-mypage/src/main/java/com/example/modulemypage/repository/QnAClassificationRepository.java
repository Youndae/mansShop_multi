package com.example.modulemypage.repository;

import com.example.modulecommon.model.entity.QnAClassification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnAClassificationRepository extends JpaRepository<QnAClassification,Long>, QnAClassificationDSLRepository {
}

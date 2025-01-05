package com.example.moduleproduct.repository.classification;

import com.example.modulecommon.model.entity.Classification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassificationRepository extends JpaRepository<Classification, String> {
}

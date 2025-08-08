package com.example.modulemypage.repository;

import com.example.modulemypage.model.dto.memberQnA.out.QnAClassificationDTO;

import java.util.List;

public interface QnAClassificationDSLRepository {

    List<QnAClassificationDTO> getAllQnAClassificationDTO();
}

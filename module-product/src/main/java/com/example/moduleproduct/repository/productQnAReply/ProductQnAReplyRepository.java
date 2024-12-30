package com.example.moduleproduct.repository.productQnAReply;

import com.example.modulecommon.model.entity.ProductQnAReply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductQnAReplyRepository extends JpaRepository<ProductQnAReply, Long>, ProductQnAReplyDSLRepository {
}

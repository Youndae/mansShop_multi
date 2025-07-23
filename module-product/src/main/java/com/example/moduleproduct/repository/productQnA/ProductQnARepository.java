package com.example.moduleproduct.repository.productQnA;

import com.example.modulecommon.model.entity.ProductQnA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductQnARepository extends JpaRepository<ProductQnA, Long>, ProductQnADSLRepository {
    //ProductWriteUseCase Integration test QueryMethod
    List<ProductQnA> findAllByMember_UserIdOrderByIdDesc(String memberUserId);
}

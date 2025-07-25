package com.example.moduleuser.repository;

import com.example.modulecommon.model.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<Auth, Long> {
}

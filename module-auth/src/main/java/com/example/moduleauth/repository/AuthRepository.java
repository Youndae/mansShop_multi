package com.example.moduleauth.repository;

import com.example.modulecommon.model.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<Auth, Long> {
}

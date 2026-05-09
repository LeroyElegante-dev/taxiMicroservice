package com.taxi.userservice.repository;

import com.taxi.userservice.entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    Optional<UserAccountEntity> findByUsernameIgnoreCase(String username);
}

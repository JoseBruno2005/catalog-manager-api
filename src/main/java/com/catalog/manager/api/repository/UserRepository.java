package com.catalog.manager.api.repository;

import com.catalog.manager.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    User findByEmailUser(@Param("email") String email);
}

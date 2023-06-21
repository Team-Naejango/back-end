package com.example.naejango.domain.user.repository;

import com.example.naejango.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserKey(String userKey);
    void deleteUserById(long id);
}

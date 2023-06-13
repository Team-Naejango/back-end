package com.example.naejango.domain.user.repository;

import com.example.naejango.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserKey(String userKey);
    void deleteUserById(long id);
}

package com.example.naejango.domain.user.repository;

import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"userProfile"})
    Optional<User> findUserWithProfileById(Long id);
    void deleteUserById(long id);
    Optional<User> findByUserKey(String userKey);
    Optional<User> findByRole(Role role);
}

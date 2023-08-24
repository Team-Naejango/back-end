package com.example.naejango.domain.user.repository;

import com.example.naejango.domain.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    @Query("SELECT up FROM User u JOIN u.userProfile up WHERE u.id = :userId")
    Optional<UserProfile> findUserProfileByUserId(@Param("userId") Long UserId);
}

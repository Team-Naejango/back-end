package com.example.naejango.domain.user.repository;

import com.example.naejango.domain.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}

package com.pulsepass.pulsepass.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.pulsepass.pulsepass.domain.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long>{

}

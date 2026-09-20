package com.pulsepass.pulsepass.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsepass.pulsepass.domain.User;

public interface UserRepository  extends JpaRepository<User, Long> {

}

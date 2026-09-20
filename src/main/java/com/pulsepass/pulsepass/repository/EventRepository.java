package com.pulsepass.pulsepass.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsepass.pulsepass.domain.Event;

public interface EventRepository extends JpaRepository<Event, Long>{

}

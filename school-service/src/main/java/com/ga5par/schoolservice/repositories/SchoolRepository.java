package com.ga5par.schoolservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ga5par.schoolservice.model.School;

public interface SchoolRepository extends JpaRepository<School,Long> {
	
	
}

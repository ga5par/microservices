package com.ga5par.studentservice.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ga5par.studentservice.model.Student;

public interface StudentRepository extends JpaRepository<Student,Long> {

	List<Student> findByIdSchool(Long id);
	
}

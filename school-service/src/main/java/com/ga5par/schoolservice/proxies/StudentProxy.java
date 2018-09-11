package com.ga5par.schoolservice.proxies;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ga5par.schoolservice.model.Student;

@FeignClient(name="student-service")
public interface StudentProxy {
	
	@GetMapping("/api/student/school/{id}")
	public List<Student> findByIdSchool(@PathVariable Long id);

}

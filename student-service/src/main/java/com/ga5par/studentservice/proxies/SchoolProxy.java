package com.ga5par.studentservice.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ga5par.studentservice.model.School;

@FeignClient(name="school-service")
public interface SchoolProxy {
	
	@GetMapping("/api/school/{id}")
	public School findById(@PathVariable Long id);

}

package com.ga5par.schoolservice.controller;

import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ga5par.schoolservice.model.School;
import com.ga5par.schoolservice.model.Student;
import com.ga5par.schoolservice.proxies.StudentProxy;
import com.ga5par.schoolservice.repositories.SchoolRepository;

@RestController
@RequestMapping("/api/school")
public class SchoolController {

//	@Autowired
	private SchoolRepository schoolRepository;

//	@Autowired
	private Environment environment;

//	@Autowired
	private StudentProxy studentProxy;

	public SchoolController(StudentProxy studentProxy, Environment environment, SchoolRepository schoolRepository) {
		this.schoolRepository=schoolRepository;
		this.environment= environment;
		this.studentProxy=studentProxy;
	}

	@GetMapping
	public List<School> findall() {
		return schoolRepository.findAll();
	}

	@GetMapping("/{id}")
	public School findById(@PathVariable Long id) {

		School school = schoolRepository.findById(id).get();
		if (!school.equals(null)) {
			Integer port = Integer.parseInt(environment.getProperty("local.server.port"));
			school.setPortSchool(port);
			return school;
		}
		return null;
	}

	@GetMapping("/{id}/with-students")
	public School withStudents(@PathVariable Long id) {

		School school = schoolRepository.findById(id).get();
		if (!school.equals(null)) {
			Integer port = Integer.parseInt(environment.getProperty("local.server.port"));
			school.setPortSchool(port);
			List<Student> studentList = studentProxy.findByIdSchool(id);
			school.setStudents(studentList);
			return school;
		}
		return null;
	}

	@PostMapping
	public School save(@RequestBody School instance) {
		return schoolRepository.save(instance);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		schoolRepository.deleteById(id);
	}

}

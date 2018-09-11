package com.ga5par.studentservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ga5par.studentservice.model.School;
import com.ga5par.studentservice.model.Student;
import com.ga5par.studentservice.proxies.SchoolProxy;
import com.ga5par.studentservice.repositories.StudentRepository;

@RestController
@RequestMapping("/api/student")
public class StudentController {

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private SchoolProxy schoolProxy;

	@Autowired
	Environment environment;

	@GetMapping
	public List<Student> findAll() {
		return studentRepository.findAll();
	}

	@GetMapping("/{id}")
	public Student findById(@PathVariable Long id) {
		Student student = studentRepository.findById(id).get();
		if (!student.equals(null)) {
			Integer port = Integer.parseInt(environment.getProperty("local.server.port"));
			student.setPortStudent(port);
			return student;
		}

		return null;
	}

	@GetMapping("/{id}/with-school")
	public Student withSchool(@PathVariable Long id) {
		Student student = studentRepository.findById(id).get();
		if (!student.equals(null)) {
			Integer port = Integer.parseInt(environment.getProperty("local.server.port"));
			student.setPortStudent(port);
			School school = schoolProxy.findById(student.getIdSchool());
			student.setSchool(school);
			return student;
		}

		return null;
	}

	@GetMapping("/school/{id}")
	public List<Student> findByIdSchool(@PathVariable Long id) {
		return studentRepository.findByIdSchool(id);
	}

	@PostMapping
	public Student save(@RequestBody Student instance) {
		return studentRepository.save(instance);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		studentRepository.deleteById(id);
	}
}

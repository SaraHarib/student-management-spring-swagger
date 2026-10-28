package com.example.student_management.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.example.student_management.entity.Student;
import com.example.student_management.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Student save(Student student) {
        return repository.save(student);
    }

    @Transactional
    public Optional<Student> update(int id, Student student) {
        return repository.findById(id).map(existing -> {
            existing.setNom(student.getNom());
            existing.setPrenom(student.getPrenom());
            existing.setDateNaissance(student.getDateNaissance());
            return repository.save(existing);
        });
    }

    @Transactional
    public boolean delete(int id) {
        Optional<Student> student = repository.findById(id);
        if (student.isEmpty()) {
            return false;
        }
        repository.delete(student.get());
        return true;
    }

    public List<Student> findAll() {
        return repository.findAll();
    }

    public long countStudents() {
        return repository.count();
    }

    public Collection<Object[]> findNbrStudentByYear() {
        return repository.findNbrStudentByYear();
    }

    public Optional<Student> findById(int id) {
        return repository.findById(id);
    }
}

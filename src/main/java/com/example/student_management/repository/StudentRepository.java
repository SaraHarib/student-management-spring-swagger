package com.example.student_management.repository;

import java.util.List;

import com.example.student_management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    @Query("""
           SELECT YEAR(s.dateNaissance), COUNT(s)
           FROM Student s
           GROUP BY YEAR(s.dateNaissance)
           ORDER BY YEAR(s.dateNaissance)
           """)
    List<Object[]> findNbrStudentByYear();
}

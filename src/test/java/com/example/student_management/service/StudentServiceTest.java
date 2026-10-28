package com.example.student_management.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.student_management.entity.Student;
import com.example.student_management.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository repository;

    @InjectMocks
    private StudentService service;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student("LACHGAR", "Mohamed", LocalDate.parse("1985-09-01"));
        student.setId(1);
    }

    @Test
    void testSaveStudent() {
        when(repository.save(student)).thenReturn(student);
        assertSame(student, service.save(student));
        verify(repository).save(student);
    }

    @Test
    void testDeleteStudent() {
        when(repository.findById(1)).thenReturn(Optional.of(student));
        assertTrue(service.delete(1));
        verify(repository).delete(student);
    }

    @Test
    void testDeleteStudentNotFound() {
        when(repository.findById(999)).thenReturn(Optional.empty());
        assertFalse(service.delete(999));
        verify(repository, never()).delete(any(Student.class));
    }

    @Test
    void testFindAllStudents() {
        when(repository.findAll()).thenReturn(List.of(student));
        assertEquals(List.of(student), service.findAll());
    }

    @Test
    void testCountStudents() {
        when(repository.count()).thenReturn(3L);
        assertEquals(3L, service.countStudents());
    }

    @Test
    void testFindByYear() {
        List<Object[]> statistics = List.of(new Object[]{1985, 2L}, new Object[]{1990, 1L});
        when(repository.findNbrStudentByYear()).thenReturn(statistics);
        assertSame(statistics, service.findNbrStudentByYear());
    }

    @Test
    void testFindStudentById() {
        when(repository.findById(1)).thenReturn(Optional.of(student));
        assertEquals(Optional.of(student), service.findById(1));
    }

    @Test
    void testFindStudentByIdNotFound() {
        when(repository.findById(999)).thenReturn(Optional.empty());
        assertTrue(service.findById(999).isEmpty());
    }

    @Test
    void testUpdateStudentPreservesId() {
        Student changes = new Student("ALAMI", "Sara", LocalDate.parse("1990-01-15"));
        changes.setId(999);
        when(repository.findById(1)).thenReturn(Optional.of(student));
        when(repository.save(student)).thenReturn(student);

        Student updated = service.update(1, changes).orElseThrow();

        assertEquals(1, updated.getId());
        assertEquals("ALAMI", updated.getNom());
        assertEquals("Sara", updated.getPrenom());
        assertEquals(changes.getDateNaissance(), updated.getDateNaissance());
        verify(repository).save(student);
    }

    @Test
    void testUpdateStudentNotFoundDoesNotInsert() {
        when(repository.findById(999)).thenReturn(Optional.empty());
        assertTrue(service.update(999, student).isEmpty());
        verify(repository, never()).save(any(Student.class));
    }
}

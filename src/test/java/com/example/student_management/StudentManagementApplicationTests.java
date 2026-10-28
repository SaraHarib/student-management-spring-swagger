package com.example.student_management;

import com.example.student_management.controller.StudentController;
import com.example.student_management.repository.StudentRepository;
import com.example.student_management.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentManagementApplicationTests {

    @Test
    void contextLoads() {
        // SpringExtension de Spring 7 exige JUnit 6 : démarrage explicite pour conserver JUnit 5.
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(StudentManagementApplication.class)
                .web(WebApplicationType.NONE)
                .run()) {
            assertNotNull(context.getBean(StudentController.class));
            assertNotNull(context.getBean(StudentService.class));
            StudentRepository repository = context.getBean(StudentRepository.class);
            assertTrue(repository.count() >= 0);
            assertNotNull(repository.findNbrStudentByYear());
        }
    }
}

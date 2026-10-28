package com.example.student_management.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.example.student_management.entity.Student;
import com.example.student_management.service.StudentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    private static final String VALID_JSON = """
            {"nom":"LACHGAR","prenom":"Mohamed","dateNaissance":"1985-09-01"}
            """;

    @Mock
    private StudentService service;

    @InjectMocks
    private StudentController controller;

    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;
    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student("LACHGAR", "Mohamed", LocalDate.parse("1985-09-01"));
        student.setId(1);
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        // Vraie couche HTTP/validation, service simulé : aucun contexte Boot ni accès SQL.
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void testSaveStudent() throws Exception {
        when(service.save(any(Student.class))).thenReturn(student);

        mvc.perform(post("/students/save").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/students/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("LACHGAR"))
                .andExpect(jsonPath("$.prenom").value("Mohamed"))
                .andExpect(jsonPath("$.dateNaissance").value("1985-09-01"));
        verify(service).save(any(Student.class));
    }

    @Test
    void testDeleteStudent() throws Exception {
        when(service.delete(1)).thenReturn(true);

        mvc.perform(delete("/students/delete/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).delete(1);
    }

    @Test
    void testDeleteStudentNotFound() throws Exception {
        when(service.delete(999)).thenReturn(false);

        mvc.perform(delete("/students/delete/999")).andExpect(status().isNotFound());
        verify(service).delete(999);
    }

    @Test
    void testFindAllStudents() throws Exception {
        when(service.findAll()).thenReturn(List.of(student));

        mvc.perform(get("/students/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("LACHGAR"));
    }

    @Test
    void testCountStudents() throws Exception {
        when(service.countStudents()).thenReturn(3L);

        mvc.perform(get("/students/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void testFindByYear() throws Exception {
        when(service.findNbrStudentByYear()).thenReturn(List.of(new Object[]{1985, 2L}, new Object[]{1990, 1L}));

        mvc.perform(get("/students/byYear"))
                .andExpect(status().isOk())
                .andExpect(content().json("[[1985,2],[1990,1]]"));
    }

    @Test
    void testFindStudentById() throws Exception {
        when(service.findById(1)).thenReturn(Optional.of(student));

        mvc.perform(get("/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dateNaissance").value("1985-09-01"));
    }

    @Test
    void testFindStudentByIdNotFound() throws Exception {
        when(service.findById(999)).thenReturn(Optional.empty());

        mvc.perform(get("/students/999")).andExpect(status().isNotFound());
    }

    @Test
    void testUpdateStudent() throws Exception {
        when(service.update(eq(1), any(Student.class))).thenReturn(Optional.of(student));

        mvc.perform(put("/students/1").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testUpdateStudentNotFound() throws Exception {
        when(service.update(eq(999), any(Student.class))).thenReturn(Optional.empty());

        mvc.perform(put("/students/999").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isNotFound());
    }

    static Stream<Arguments> invalidStudents() {
        return Stream.of(
                Arguments.of("{\"prenom\":\"Mohamed\",\"dateNaissance\":\"1985-09-01\"}", "nom"),
                Arguments.of("{\"nom\":\"   \",\"prenom\":\"Mohamed\",\"dateNaissance\":\"1985-09-01\"}", "nom"),
                Arguments.of("{\"nom\":\"LACHGAR\",\"dateNaissance\":\"1985-09-01\"}", "prenom"),
                Arguments.of("{\"nom\":\"LACHGAR\",\"prenom\":\"\",\"dateNaissance\":\"1985-09-01\"}", "prenom"),
                Arguments.of("{\"nom\":\"LACHGAR\",\"prenom\":\"Mohamed\"}", "dateNaissance"),
                Arguments.of("{\"nom\":\"LACHGAR\",\"prenom\":\"Mohamed\",\"dateNaissance\":null}", "dateNaissance"));
    }

    @ParameterizedTest
    @MethodSource("invalidStudents")
    void testInvalidStudentRejected(String json, String field) throws Exception {
        mvc.perform(post("/students/save").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors." + field).isNotEmpty());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-json", "{\"nom\":\"A\",\"prenom\":\"B\",\"dateNaissance\":\"1985-02-30\"}"})
    void testMalformedJsonOrDateRejected(String json) throws Exception {
        mvc.perform(post("/students/save").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requête JSON invalide"));
        verifyNoInteractions(service);
    }

    @Test
    void testInvalidUpdateRejected() throws Exception {
        mvc.perform(put("/students/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void testPostCannotOverwriteExistingId() throws Exception {
        when(service.save(any(Student.class))).thenAnswer(invocation -> {
            Student submitted = invocation.getArgument(0);
            assertEquals(0, submitted.getId());
            return student;
        });

        mvc.perform(post("/students/save").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":999,\"nom\":\"LACHGAR\",\"prenom\":\"Mohamed\",\"dateNaissance\":\"1985-09-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}

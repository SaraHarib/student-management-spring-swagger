package com.example.student_management.controller;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import com.example.student_management.entity.Student;
import com.example.student_management.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@Tag(name = "StudentController", description = "Gestion des étudiants — TP6")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @PostMapping("/save")
    @Operation(summary = "Ajouter un étudiant")
    @ApiResponse(responseCode = "201", description = "Étudiant créé")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    public ResponseEntity<Student> save(@Valid @RequestBody Student student) {
        student.setId(0);
        Student saved = service.save(student);
        return ResponseEntity.created(URI.create("/students/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un étudiant existant")
    @ApiResponse(responseCode = "200", description = "Étudiant modifié")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "404", description = "Étudiant introuvable")
    public ResponseEntity<Student> update(@PathVariable int id, @Valid @RequestBody Student student) {
        return ResponseEntity.of(service.update(id, student));
    }

    @GetMapping("/all")
    @Operation(summary = "Lister tous les étudiants")
    @ApiResponse(responseCode = "200", description = "Liste des étudiants")
    public ResponseEntity<List<Student>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/count")
    @Operation(summary = "Compter les étudiants")
    @ApiResponse(responseCode = "200", description = "Nombre total d'étudiants")
    public ResponseEntity<Long> countStudents() {
        return ResponseEntity.ok(service.countStudents());
    }

    @GetMapping("/byYear")
    @Operation(summary = "Compter les étudiants par année de naissance",
            description = "Liste de couples [année, nombre], triée par année croissante.")
    @ApiResponse(responseCode = "200", description = "Statistiques par année")
    public ResponseEntity<Collection<Object[]>> findNbrStudentByYear() {
        return ResponseEntity.ok(service.findNbrStudentByYear());
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Supprimer un étudiant")
    @ApiResponse(responseCode = "204", description = "Étudiant supprimé")
    @ApiResponse(responseCode = "404", description = "Étudiant introuvable")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Rechercher un étudiant par identifiant")
    @ApiResponse(responseCode = "200", description = "Étudiant trouvé")
    @ApiResponse(responseCode = "404", description = "Étudiant introuvable")
    public ResponseEntity<Student> findById(@PathVariable int id) {
        return ResponseEntity.of(service.findById(id));
    }
}

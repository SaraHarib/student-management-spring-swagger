package com.example.student_management.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private int id;

    @NotBlank(message = "Le nom est obligatoire")
    @Schema(example = "LACHGAR")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Schema(example = "Mohamed")
    private String prenom;

    @NotNull(message = "La date de naissance est obligatoire")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd", lenient = com.fasterxml.jackson.annotation.OptBoolean.FALSE)
    @Schema(type = "string", format = "date", example = "1985-09-01")
    private LocalDate dateNaissance;

    public Student() {
    }

    public Student(String nom, String prenom, LocalDate dateNaissance) {
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", nom='" + nom + "', prenom='" + prenom
                + "', dateNaissance=" + dateNaissance + "}";
    }
}

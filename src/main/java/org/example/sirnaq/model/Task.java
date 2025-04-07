package org.example.sirnaq.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

@Entity
public class Task implements Serializable {

    @Id
    private Long id;
    @NotNull(message="Title cannot be null!")
    private String title;
    private boolean completed;

    //Konstruktory gettery i settery

    public Task() {} // Domyślny konstruktor wymagany przez JPA

    public Task(Long id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

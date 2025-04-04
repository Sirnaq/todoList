package org.example.sirnaq;

import org.example.sirnaq.model.Task;
import org.example.sirnaq.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void testCreateAndGetTask() throws Exception {
        //Czyszczenie bazy przed testem
        taskRepository.deleteAll();

        //Post - dodaj zadanie
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1, \"title\": \"Test Task\", \"completed\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));

        //get - sprawdź listę zadań
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].completed").value(false));

    }

    @Test
    public void testUpdateTask() throws Exception {
        // Dodajemy zadanie do bazy
        taskRepository.save(new Task(1L, "Old Task", false));

        //put - modyfikuj zadanie
        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1, \"title\": \"Updated task\", \"completed\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated task"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    public void taskToDelete() throws Exception {
        // Dodajemy zadanie do bazy
        taskRepository.save(new Task(1L, "Task to delete", false));

        //delete - usuwamy zadanie
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isOk());

        // Sprawdzamy czy zadanie zostało usunięte
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }


}

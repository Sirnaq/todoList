package org.example.sirnaq;

import org.example.sirnaq.model.Task;
import org.example.sirnaq.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testCreateTask() throws Exception {
        // Wykonaj żądanie POST
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1, \"title\": \"Test Task\", \"completed\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));

        // Przechwyć argument przekazany do RabbitTemplate
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        Mockito.verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.TASK_QUEUE), taskCaptor.capture());

        // Sprawdź wartości pól przechwyconego obiektu
        Task capturedTask = taskCaptor.getValue();
        assertEquals(1L, capturedTask.getId());
        assertEquals("Test Task", capturedTask.getTitle());
        assertFalse(capturedTask.isCompleted());
    }


    @Test
    public void testCreateAndGetTask() throws Exception {
        // Czyszczenie bazy przed testem
        taskRepository.deleteAll();

        // POST - dodaj zadanie
        Task task = new Task(1L, "Test Task", false);
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1, \"title\": \"Test Task\", \"completed\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));

        // Symulujemy zapis zadania do bazy (konsument RabbitMQ)
        taskRepository.save(task);

        // GET - sprawdź listę zadań
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    public void testUpdateTask() throws Exception {
        //czyszczenie bazy przed testem
        taskRepository.deleteAll();

        //dodajemy zadanie do bazy
        taskRepository.save(new Task(1L, "Old Task", false));

        //put - modyfikuj zadanie
        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1, \"title\": \"Updated task\", \"completed\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated task"))
                .andExpect(jsonPath("$.completed").value(true));

        //sprawdzamy czy zadanie zostało zmodysikowane
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Updated task"))
                .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test
    public void testTaskToDelete() throws Exception {

        //czyszczenie bazy przed testem
        taskRepository.deleteAll();

        //dodajemy zadanie do bazy
        taskRepository.save(new Task(1L, "Task to delete", false));

        //delete - usuwamy zadanie
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());

        //sprawdzamy czy zadanie zostało usunięte
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void testUpdateTaskNotFound() throws Exception {
        //czyszczenie bazy przed testem
        taskRepository.deleteAll();

        //modyfikujemy zadanie które nie istnieje
        mockMvc.perform(put("/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 999,\"title\": \"Non-existent\", \"completed\": false}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteTaskNotFound() throws Exception {
        //czyszczenie bazy przed testem
        taskRepository.deleteAll();

        //usuwamy zadanie które nie istnieje
        mockMvc.perform(delete("/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPostTaskValidation() throws Exception {
        //czyszczenie bazy przed testem
        taskRepository.deleteAll();

        //dodajemy zadanie bez nazwy
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1, \"completed\": true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetFilteredResult() throws Exception {
        //czyszczenie bazy przed testem
        taskRepository.deleteAll();

        //dodajemy dwa zadania do bazy
        taskRepository.save(new Task(0L, "Completed task", true));
        taskRepository.save(new Task(1L, "Uncompleted task", false));

        //get - sprawdzamy przefiltrowane wyniki - zakończone
        mockMvc.perform(get("/tasks?completed=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Completed task"))
                .andExpect(jsonPath("$[0].completed").value(true));

        //get - sprawdzamy przefiltrowane wyniki - niezakończone
        mockMvc.perform(get("/tasks?completed=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Uncompleted task"))
                .andExpect(jsonPath("$[0].completed").value(false));

    }


}

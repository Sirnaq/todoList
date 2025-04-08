package org.example.sirnaq;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.example.sirnaq.model.Task;
import org.example.sirnaq.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        taskRepository.deleteAll(); //Czyszczenie bazy przed każdym testem
    }

    @Test
    public void testCrateAndGetTask() throws InterruptedException {
        // POST - dodaj zadanie
        Task task = new Task(1L, "Integration task", false);
        given()
                .contentType(ContentType.JSON)
                .body(task)
                .when()
                .post("/tasks")
                .then()
                .statusCode(200)
                .body("title", equalTo("Integration task"))
                .body("completed", equalTo(false));

        // Czekaj aż w db pojawi się zadanie
        await().atMost(5, TimeUnit.SECONDS).until(() -> !taskRepository.findAll().isEmpty());

        // GET - sprawdź listę zadań
        given()
                .when()
                .get("/tasks")
                .then()
                .statusCode(200)
                .body("[0].id", equalTo(1))
                .body("[0].title", equalTo("Integration task"))
                .body("[0].completed", equalTo(false));
    }


}

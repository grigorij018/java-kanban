package test;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.manager.InMemoryTaskManager;
import ru.common.manager.TaskManager;
import ru.common.model.Epic;
import ru.common.model.SubTask;
import ru.common.model.Task;
import ru.common.model.TaskStatus;
import ru.common.server.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private Gson gson;
    private HttpClient client;
    private static int testCounter = 0;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        gson = server.getGson(); // Используем гсон из сервера
        client = HttpClient.newHttpClient();

        // Даем небольшую задержку между тестами
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void testGetTasks() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description");
        manager.createTask(task);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, tasks.length);
        assertEquals("Test Task", tasks[0].getName());
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description");
        Task created = manager.createTask(task);

        URI url = URI.create("http://localhost:8080/tasks/" + created.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task retrieved = gson.fromJson(response.body(), Task.class);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals("Test Task", retrieved.getName());
    }

    @Test
    void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("New Task", "New Description",
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("New Task", tasks.get(0).getName());
    }

    @Test
    void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Original Task", "Original Description");
        Task created = manager.createTask(task);
        created.setName("Updated Task");

        String taskJson = gson.toJson(created);
        URI url = URI.create("http://localhost:8080/tasks/" + created.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task updated = manager.getTask(created.getId());
        assertEquals("Updated Task", updated.getName());
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description");
        Task created = manager.createTask(task);

        URI url = URI.create("http://localhost:8080/tasks/" + created.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(manager.getTask(created.getId()));
    }

    @Test
    void testCreateEpicWithSubtasks() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Test Description");
        String epicJson = gson.toJson(epic);

        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode());
        Epic createdEpic = gson.fromJson(epicResponse.body(), Epic.class);

        // Создаем подзадачу
        SubTask subtask = new SubTask("Test Subtask", "Test Description",
                createdEpic.getId(), Duration.ofMinutes(30),
                LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        URI subtaskUrl = URI.create("http://localhost:8080/subtasks");
        HttpRequest subtaskRequest = HttpRequest.newBuilder()
                .uri(subtaskUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> subtaskResponse = client.send(subtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, subtaskResponse.statusCode());

        // Проверяем подзадачи эпика
        URI epicSubtasksUrl = URI.create("http://localhost:8080/epics/" + createdEpic.getId() + "/subtasks");
        HttpRequest epicSubtasksRequest = HttpRequest.newBuilder().uri(epicSubtasksUrl).GET().build();
        HttpResponse<String> epicSubtasksResponse = client.send(epicSubtasksRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, epicSubtasksResponse.statusCode());
        SubTask[] subtasks = gson.fromJson(epicSubtasksResponse.body(), SubTask[].class);
        assertEquals(1, subtasks.length);
    }

    @Test
    void testGetHistory() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description");
        Task created = manager.createTask(task);
        manager.getTask(created.getId()); // Добавляем в историю

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, history.length);
        assertEquals(created.getId(), history[0].getId());
    }

    @Test
    void testGetPrioritized() throws IOException, InterruptedException {
        // Используем разные временные интервалы чтобы избежать пересечений
        LocalDateTime baseTime = LocalDateTime.now();

        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(1), baseTime.plusHours(2)); // Начинается через 2 часа
        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), baseTime); // Начинается сейчас

        manager.createTask(task1);
        manager.createTask(task2);

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, prioritized.length);
        assertEquals("Task 2", prioritized[0].getName()); // Более ранняя задача должна быть первой
        assertEquals("Task 1", prioritized[1].getName()); // Более поздняя задача должна быть второй
    }

    @Test
    void testTaskNotFound() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testTimeOverlap() throws IOException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(2), now);
        manager.createTask(task1);

        // Создаем задачу, которая пересекается по времени
        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), now.plusMinutes(30));
        String taskJson = gson.toJson(task2);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode()); // Должен вернуть ошибку пересечения
    }
}
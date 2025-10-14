package test;

import org.junit.jupiter.api.Test;
import ru.common.manager.InMemoryTaskManager;
import ru.common.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeOverlapTest {

    @Test
    void shouldDetectOverlappingTasks() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration oneHour = Duration.ofHours(1);

        Task task1 = new Task("Task1", "Desc", oneHour, baseTime);
        Task task2 = new Task("Task2", "Desc", oneHour, baseTime.plusMinutes(30));

        // Создаем простой менеджер для теста пересечений
        InMemoryTaskManager manager = new InMemoryTaskManager();
        manager.createTask(task1);

        assertThrows(IllegalArgumentException.class, () -> manager.createTask(task2));
    }

    @Test
    void shouldAllowNonOverlappingTasks() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration oneHour = Duration.ofHours(1);

        Task task1 = new Task("Task1", "Desc", oneHour, baseTime);
        Task task2 = new Task("Task2", "Desc", oneHour, baseTime.plusHours(2));

        InMemoryTaskManager manager = new InMemoryTaskManager();
        manager.createTask(task1);

        assertDoesNotThrow(() -> manager.createTask(task2));
    }
}

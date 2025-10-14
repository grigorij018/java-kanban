package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.manager.Managers;
import ru.common.manager.TaskManager;
import ru.common.model.Epic;
import ru.common.model.SubTask;
import ru.common.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends test.TaskManagerTest<TaskManager> {

    @Override
    protected TaskManager createTaskManager() {
        return Managers.getDefault();
    }

    @BeforeEach
    void setUp() {
        manager = createTaskManager();
    }

    // Специфичные тесты для InMemoryTaskManager
    @Test
    void shouldDetectTimeOverlap() {
        LocalDateTime now = LocalDateTime.now();
        Duration oneHour = Duration.ofHours(1);

        Task task1 = new Task("Task1", "Desc", oneHour, now);
        manager.createTask(task1);

        Task task2 = new Task("Task2", "Desc", oneHour, now.plusMinutes(30)); // Пересекается

        assertThrows(IllegalArgumentException.class, () -> manager.createTask(task2));
    }

    // Оригинальные тесты из вашего файла
    @Test
    void shouldAddAndFindDifferentTaskTypes() {
        Task task = new Task("Task", "Description");
        Epic epic = new Epic("Epic", "Description");
        SubTask subTask = new SubTask("Subtask", "Description", 2); // epicId=2

        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubtask(subTask);

        assertNotNull(manager.getTask(task.getId()));
        assertNotNull(manager.getEpic(epic.getId()));
        assertNotNull(manager.getSubtask(subTask.getId()));
    }

    @Test
    void generatedAndManualIdsShouldNotConflict() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description");
        task2.setId(1);

        manager.createTask(task1);
        manager.createTask(task2);

        assertEquals(2, manager.getAllTasks().size(), "Должны быть 2 задачи, несмотря на одинаковые ID");
    }

    @Test
    void shouldRemoveTaskFromHistoryWhenDeleted() {
        Task task = new Task("Task", "Description");
        manager.createTask(task);
        manager.getTask(task.getId()); // Добавляем в историю
        manager.deleteTask(task.getId());

        assertTrue(manager.getHistory().isEmpty(), "Задача должна удаляться из истории при удалении");
    }

    @Test
    void shouldRemoveEpicAndSubtasksFromHistoryWhenEpicDeleted() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);
        SubTask subTask = new SubTask("Subtask", "Description", epic.getId());
        manager.createSubtask(subTask);

        manager.getEpic(epic.getId());
        manager.getSubtask(subTask.getId()); // Добавляем в историю

        manager.deleteEpic(epic.getId());

        assertTrue(manager.getHistory().isEmpty(), "Эпик и подзадачи должны удаляться из истории");
    }
}
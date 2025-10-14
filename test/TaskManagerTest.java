package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.manager.TaskManager;
import ru.common.model.Epic;
import ru.common.model.SubTask;
import ru.common.model.Task;
import ru.common.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        manager = createTaskManager();
    }

    @Test
    void shouldAddAndFindTask() {
        Task task = new Task("Test Task", "Test Description");
        Task created = manager.createTask(task);

        assertNotNull(created.getId());
        assertEquals(task.getName(), created.getName());
        assertEquals(task.getDescription(), created.getDescription());
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Test Task", "Test Description");
        Task created = manager.createTask(task);

        created.setName("Updated Task");
        created.setDescription("Updated Description");
        manager.updateTask(created);

        Task updated = manager.getTask(created.getId());
        assertEquals("Updated Task", updated.getName());
        assertEquals("Updated Description", updated.getDescription());
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task("Test Task", "Test Description");
        Task created = manager.createTask(task);

        manager.deleteTask(created.getId());
        assertNull(manager.getTask(created.getId()));
    }

    // Тесты для Epic статусов
    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);

        SubTask sub1 = new SubTask("Sub1", "Desc1", epic.getId());
        SubTask sub2 = new SubTask("Sub2", "Desc2", epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(TaskStatus.NEW, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);

        SubTask sub1 = new SubTask("Sub1", "Desc1", epic.getId());
        SubTask sub2 = new SubTask("Sub2", "Desc2", epic.getId());
        sub1.setStatus(TaskStatus.DONE);
        sub2.setStatus(TaskStatus.DONE);

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(TaskStatus.DONE, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksMixed() {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);

        SubTask sub1 = new SubTask("Sub1", "Desc1", epic.getId());
        SubTask sub2 = new SubTask("Sub2", "Desc2", epic.getId());
        sub1.setStatus(TaskStatus.NEW);
        sub2.setStatus(TaskStatus.DONE);

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenAnySubtaskInProgress() {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.createEpic(epic);

        SubTask sub1 = new SubTask("Sub1", "Desc1", epic.getId());
        SubTask sub2 = new SubTask("Sub2", "Desc2", epic.getId());
        sub1.setStatus(TaskStatus.IN_PROGRESS);
        sub2.setStatus(TaskStatus.DONE);

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());
    }

    // Тесты для времени и продолжительности
    @Test
    void shouldCalculateTaskEndTime() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(2);
        Task task = new Task("Test", "Desc", duration, startTime);

        assertEquals(startTime.plus(duration), task.getEndTime());
    }

    @Test
    void shouldHandleTasksWithoutTime() {
        Task task = new Task("Test", "Desc");
        assertNull(task.getStartTime());
        assertNull(task.getEndTime());
    }

    // Тесты для приоритетов
    @Test
    void shouldReturnPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Task1", "Desc", Duration.ofHours(1), now.plusHours(2));
        Task task2 = new Task("Task2", "Desc", Duration.ofHours(1), now.plusHours(1));

        manager.createTask(task2); // Более ранняя задача
        manager.createTask(task1); // Более поздняя задача

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertEquals("Task2", prioritized.get(0).getName()); // Должна быть первой
        assertEquals("Task1", prioritized.get(1).getName()); // Должна быть второй
    }

    @Test
    void shouldNotIncludeTasksWithoutTimeInPrioritized() {
        Task taskWithTime = new Task("With Time", "Desc", Duration.ofHours(1), LocalDateTime.now());
        Task taskWithoutTime = new Task("Without Time", "Desc");

        manager.createTask(taskWithTime);
        manager.createTask(taskWithoutTime);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(1, prioritized.size());
        assertEquals("With Time", prioritized.get(0).getName());
    }
}
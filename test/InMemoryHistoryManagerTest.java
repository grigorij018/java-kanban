package test;

import org.junit.jupiter.api.Test;
import ru.common.manager.HistoryManager;
import ru.common.manager.Managers;
import ru.common.model.Task;
import ru.common.model.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void shouldPreserveTaskDataInHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task originalTask = new Task("Original", "Description");
        originalTask.setStatus(TaskStatus.IN_PROGRESS);

        historyManager.add(originalTask);
        List<Task> history = historyManager.getHistory();
        Task savedTask = history.get(0);

        assertEquals(originalTask.getName(), savedTask.getName());
        assertEquals(originalTask.getDescription(), savedTask.getDescription());
        assertEquals(originalTask.getStatus(), savedTask.getStatus());
    }

    @Test
    void shouldNotDuplicateTasksInHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Task", "Description");
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task); // Дублирующее добавление

        assertEquals(1, historyManager.getHistory().size(), "История не должна содержать дубликатов");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Task", "Description");
        task.setId(1);

        historyManager.add(task);
        historyManager.remove(1);

        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой после удаления");
    }

    @Test
    void shouldMaintainInsertionOrder() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task1 = new Task("Task1", "Description");
        Task task2 = new Task("Task2", "Description");
        task1.setId(1);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.get(0).getId());
        assertEquals(2, history.get(1).getId());
    }
}

package ru.common.Test;

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
    void shouldStoreLast10TasksInHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description");
            historyManager.add(task);
        }

        assertEquals(10, historyManager.getHistory().size(), "История должна содержать только 10 последних задач");
    }
}

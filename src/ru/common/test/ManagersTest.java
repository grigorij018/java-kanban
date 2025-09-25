package ru.common.test;

import org.junit.jupiter.api.Test;
import ru.common.manager.HistoryManager;
import ru.common.manager.Managers;
import ru.common.manager.TaskManager;
import ru.common.model.Task;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void getDefaultShouldReturnInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер не должен быть null");

        Task task = new Task("Test", "Description");
        manager.createTask(task);
        assertNotNull(manager.getTask(task.getId()), "Менеджер должен возвращать добавленные задачи");
    }

    @Test
    void getDefaultHistoryShouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Менеджер истории не должен быть null");

        Task task = new Task("Test", "Description");
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "История должна содержать добавленные задачи");
    }
}
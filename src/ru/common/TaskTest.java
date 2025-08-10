package ru.common;

import org.junit.jupiter.api.Test;
import ru.common.manager.Managers;
import ru.common.manager.TaskManager;
import ru.common.model.Task;
import ru.common.model.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description");
        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    void taskShouldNotChangeAfterAddingToManager() {
        TaskManager manager = Managers.getDefault();
        Task originalTask = new Task("Original", "Description");
        originalTask.setStatus(TaskStatus.IN_PROGRESS);

        Task createdTask = manager.createTask(originalTask);

        assertEquals(originalTask.getName(), createdTask.getName());
        assertEquals(originalTask.getDescription(), createdTask.getDescription());
        assertEquals(originalTask.getStatus(), createdTask.getStatus());
    }
}
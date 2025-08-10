package ru.common.manager;

import ru.common.model.Epic;
import ru.common.model.SubTask;
import ru.common.model.Task;
import java.util.List;

public interface TaskManager {
    List<Task> getAllTasks();
    void deleteAllTasks();
    Task getTask(int id);
    Task createTask(Task task);
    void updateTask(Task task);
    void deleteTask(int id);

    List<Epic> getAllEpics();
    void deleteAllEpics();
    Epic getEpic(int id);
    Epic createEpic(Epic epic);
    void updateEpic(Epic epic);
    void deleteEpic(int id);

    List<SubTask> getAllSubtasks();
    void deleteAllSubtasks();
    SubTask getSubtask(int id);
    SubTask createSubtask(SubTask subtask);
    void updateSubtask(SubTask subtask);
    void deleteSubtask(int id);
    List<SubTask> getSubtasksByEpic(int epicId);

    List<Task> getHistory();
}
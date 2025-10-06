import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.common.manager.FileBackedTaskManager;
import ru.common.model.Epic;
import ru.common.model.SubTask;
import ru.common.model.Task;
import ru.common.model.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSaveAndLoadEmptyFile() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadTasks() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = new Task("Test Task", "Description");
        manager.createTask(task);

        Epic epic = new Epic("Test Epic", "Description");
        manager.createEpic(epic);

        SubTask subtask = new SubTask("Test Subtask", "Description", epic.getId());
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        Task loadedTask = loadedManager.getAllTasks().get(0);
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());
    }

    @Test
    void shouldHandleTaskStatusCorrectly() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task = new Task("Test Task", "Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        manager.createTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loadedManager.getAllTasks().get(0);

        assertEquals(TaskStatus.IN_PROGRESS, loadedTask.getStatus());
    }

    @Test
    void shouldSaveAndLoadEpicWithSubtasks() throws IOException {
        File file = tempDir.resolve("test.csv").toFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        SubTask subtask1 = new SubTask("Subtask 1", "Description", epic.getId());
        SubTask subtask2 = new SubTask("Subtask 2", "Description", epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Epic loadedEpic = loadedManager.getAllEpics().get(0);
        assertEquals(2, loadedEpic.getSubtaskIds().size());
        assertEquals(2, loadedManager.getSubtasksByEpic(loadedEpic.getId()).size());
    }
}
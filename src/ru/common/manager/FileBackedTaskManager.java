package ru.common.manager;

import ru.common.model.Epic;
import ru.common.model.SubTask;
import ru.common.model.Task;
import ru.common.model.TaskStatus;
import ru.common.model.TaskType;

import java.io.*;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.load();
        return manager;
    }

    private void save() {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }

            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }

            for (SubTask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private void load() {
        if (!file.exists()) {
            return;
        }

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            // Пропускаем заголовок
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.isEmpty()) {
                    Task task = fromString(line);
                    if (task != null) {
                        addTaskToManager(task);
                    }
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
    }

    private void addTaskToManager(Task task) {
        if (task instanceof Epic) {
            epics.put(task.getId(), (Epic) task);
            updateNextId(task.getId());
        } else if (task instanceof SubTask) {
            subtasks.put(task.getId(), (SubTask) task);
            Epic epic = epics.get(((SubTask) task).getEpicId());
            if (epic != null) {
                epic.addSubtaskId(task.getId());
            }
            updateNextId(task.getId());
        } else {
            tasks.put(task.getId(), task);
            updateNextId(task.getId());
        }
    }

    private void updateNextId(int id) {
        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    private String toString(Task task) {
        if (task instanceof Epic) {
            return String.format("%d,EPIC,%s,%s,%s,",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getDescription());
        } else if (task instanceof SubTask) {
            SubTask subtask = (SubTask) task;
            return String.format("%d,SUBTASK,%s,%s,%s,%d",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getDescription(),
                    subtask.getEpicId());
        } else {
            return String.format("%d,TASK,%s,%s,%s,",
                    task.getId(),
                    task.getName(),
                    task.getStatus(),
                    task.getDescription());
        }
    }

    private Task fromString(String value) {
        String[] fields = value.split(",");
        if (fields.length < 5) return null;

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK:
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                if (fields.length < 6) return null;
                int epicId = Integer.parseInt(fields[5]);
                SubTask subtask = new SubTask(name, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                return null;
        }
    }

    // Переопределяем методы для автосохранения

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public SubTask createSubtask(SubTask subtask) {
        SubTask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateSubtask(SubTask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    public static void main(String[] args) {
        // Тестовый сценарий
        try {
            File tempFile = File.createTempFile("tasks", ".csv");
            FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

            // Создаем задачи
            Task task1 = new Task("Задача 1", "Описание задачи 1");
            Task task2 = new Task("Задача 2", "Описание задачи 2");
            manager.createTask(task1);
            manager.createTask(task2);

            Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
            manager.createEpic(epic1);

            SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
            SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epic1.getId());
            manager.createSubtask(subTask1);
            manager.createSubtask(subTask2);

            // Загружаем из файла
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

            // Проверяем, что все задачи загрузились
            System.out.println("Задачи после загрузки:");
            System.out.println("Tasks: " + loadedManager.getAllTasks().size());
            System.out.println("Epics: " + loadedManager.getAllEpics().size());
            System.out.println("Subtasks: " + loadedManager.getAllSubtasks().size());

            // Чистим временный файл
            tempFile.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
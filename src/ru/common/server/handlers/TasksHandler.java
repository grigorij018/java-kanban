package ru.common.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.common.manager.TaskManager;
import ru.common.model.Task;
import ru.common.server.BaseHttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TasksHandler(TaskManager manager, Gson gson) {
        super(gson);
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String pathId = getPathId(exchange);

        if (pathId == null) {
            // GET /tasks
            List<Task> tasks = manager.getAllTasks();
            sendSuccess(exchange, toJson(tasks));
        } else {
            // GET /tasks/{id}
            try {
                int id = Integer.parseInt(pathId);
                Task task = manager.getTask(id);
                if (task != null) {
                    sendSuccess(exchange, toJson(task));
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный ID задачи");
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Optional<Task> taskOpt = parseJson(body, Task.class);

        if (taskOpt.isEmpty()) {
            sendBadRequest(exchange, "Некорректный JSON задачи");
            return;
        }

        Task task = taskOpt.get();
        String pathId = getPathId(exchange);

        try {
            if (pathId == null) {
                // POST /tasks - создание
                Task created = manager.createTask(task);
                sendCreated(exchange, toJson(created));
            } else {
                // POST /tasks/{id} - обновление
                int id = Integer.parseInt(pathId);
                if (id != task.getId()) {
                    sendBadRequest(exchange, "ID в пути и в теле запроса не совпадают");
                    return;
                }
                manager.updateTask(task);
                sendSuccess(exchange, toJson(task));
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String pathId = getPathId(exchange);
        if (pathId == null) {
            // DELETE /tasks - удаление всех задач
            manager.deleteAllTasks();
            sendSuccess(exchange, "{\"message\": \"Все задачи удалены\"}");
        } else {
            // DELETE /tasks/{id} - удаление по ID
            try {
                int id = Integer.parseInt(pathId);
                Task task = manager.getTask(id);
                if (task != null) {
                    manager.deleteTask(id);
                    sendSuccess(exchange, "{\"message\": \"Задача удалена\"}");
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный ID задачи");
            }
        }
    }
}

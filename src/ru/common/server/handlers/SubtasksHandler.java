package ru.common.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.common.manager.TaskManager;
import ru.common.model.SubTask;
import ru.common.server.BaseHttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public SubtasksHandler(TaskManager manager, Gson gson) {
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
            // GET /subtasks
            List<SubTask> subtasks = manager.getAllSubtasks();
            sendSuccess(exchange, toJson(subtasks));
        } else {
            // GET /subtasks/{id}
            try {
                int id = Integer.parseInt(pathId);
                SubTask subtask = manager.getSubtask(id);
                if (subtask != null) {
                    sendSuccess(exchange, toJson(subtask));
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный ID подзадачи");
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Optional<SubTask> subtaskOpt = parseJson(body, SubTask.class);

        if (subtaskOpt.isEmpty()) {
            sendBadRequest(exchange, "Некорректный JSON подзадачи");
            return;
        }

        SubTask subtask = subtaskOpt.get();
        String pathId = getPathId(exchange);

        try {
            if (pathId == null) {
                // POST /subtasks - создание
                SubTask created = manager.createSubtask(subtask);
                if (created != null) {
                    sendCreated(exchange, toJson(created));
                } else {
                    sendBadRequest(exchange, "Эпик для подзадачи не найден");
                }
            } else {
                // POST /subtasks/{id} - обновление
                int id = Integer.parseInt(pathId);
                if (id != subtask.getId()) {
                    sendBadRequest(exchange, "ID в пути и в теле запроса не совпадают");
                    return;
                }
                manager.updateSubtask(subtask);
                sendSuccess(exchange, toJson(subtask));
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String pathId = getPathId(exchange);
        if (pathId == null) {
            // DELETE /subtasks - удаление всех подзадач
            manager.deleteAllSubtasks();
            sendSuccess(exchange, "{\"message\": \"Все подзадачи удалены\"}");
        } else {
            // DELETE /subtasks/{id} - удаление по ID
            try {
                int id = Integer.parseInt(pathId);
                SubTask subtask = manager.getSubtask(id);
                if (subtask != null) {
                    manager.deleteSubtask(id);
                    sendSuccess(exchange, "{\"message\": \"Подзадача удалена\"}");
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный ID подзадачи");
            }
        }
    }
}
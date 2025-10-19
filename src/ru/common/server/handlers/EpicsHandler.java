package ru.common.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.common.manager.TaskManager;
import ru.common.model.Epic;
import ru.common.model.SubTask;
import ru.common.server.BaseHttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public EpicsHandler(TaskManager manager, Gson gson) {
        super(gson);
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.matches("/epics/\\d+/subtasks")) {
                // Обработка GET /epics/{id}/subtasks
                if ("GET".equals(method)) {
                    handleGetEpicSubtasks(exchange);
                } else {
                    sendNotFound(exchange);
                }
                return;
            }

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
            // GET /epics
            List<Epic> epics = manager.getAllEpics();
            sendSuccess(exchange, toJson(epics));
        } else {
            // GET /epics/{id}
            try {
                int id = Integer.parseInt(pathId);
                Epic epic = manager.getEpic(id);
                if (epic != null) {
                    sendSuccess(exchange, toJson(epic));
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный ID эпика");
            }
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        String pathId = getPathId(exchange);
        try {
            int epicId = Integer.parseInt(pathId);
            Epic epic = manager.getEpic(epicId);
            if (epic != null) {
                List<SubTask> subtasks = manager.getSubtasksByEpic(epicId);
                sendSuccess(exchange, toJson(subtasks));
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Некорректный ID эпика");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Optional<Epic> epicOpt = parseJson(body, Epic.class);

        if (epicOpt.isEmpty()) {
            sendBadRequest(exchange, "Некорректный JSON эпика");
            return;
        }

        Epic epic = epicOpt.get();
        String pathId = getPathId(exchange);

        if (pathId == null) {
            // POST /epics - создание
            Epic created = manager.createEpic(epic);
            sendCreated(exchange, toJson(created));
        } else {
            // POST /epics/{id} - обновление
            try {
                int id = Integer.parseInt(pathId);
                if (id != epic.getId()) {
                    sendBadRequest(exchange, "ID в пути и в теле запроса не совпадают");
                    return;
                }
                manager.updateEpic(epic);
                sendSuccess(exchange, toJson(epic));
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный ID эпика");
            }
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String pathId = getPathId(exchange);
        if (pathId == null) {
            // DELETE /epics - удаление всех эпиков
            manager.deleteAllEpics();
            sendSuccess(exchange, "{\"message\": \"Все эпики удалены\"}");
        } else {
            // DELETE /epics/{id} - удаление по ID
            try {
                int id = Integer.parseInt(pathId);
                Epic epic = manager.getEpic(id);
                if (epic != null) {
                    manager.deleteEpic(id);
                    sendSuccess(exchange, "{\"message\": \"Эпик удален\"}");
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный ID эпика");
            }
        }
    }
}
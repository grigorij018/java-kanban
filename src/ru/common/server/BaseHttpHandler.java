package ru.common.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.common.manager.ManagerSaveException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class BaseHttpHandler {
    protected final Gson gson;

    public BaseHttpHandler(Gson gson) {
        this.gson = gson;
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    protected void sendSuccess(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 200);
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 201);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        String response = "{\"message\": \"Ресурс не найден\"}";
        sendText(exchange, response, 404);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        String response = "{\"message\": \"Задача пересекается по времени с существующей\"}";
        sendText(exchange, response, 406);
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        String response = "{\"message\": \"Внутренняя ошибка сервера\"}";
        sendText(exchange, response, 500);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        String response = "{\"message\": \"" + message + "\"}";
        sendText(exchange, response, 400);
    }

    protected <T> Optional<T> parseJson(String json, Class<T> clazz) {
        try {
            return Optional.of(gson.fromJson(json, clazz));
        } catch (JsonSyntaxException e) {
            return Optional.empty();
        }
    }

    protected String toJson(Object object) {
        return gson.toJson(object);
    }

    protected String getPathId(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length > 2) {
            return pathParts[2];
        }
        return null;
    }

    protected void handleException(HttpExchange exchange, Exception e) throws IOException {
        if (e instanceof IllegalArgumentException) {
            sendHasInteractions(exchange);
        } else if (e instanceof ManagerSaveException) {
            sendInternalError(exchange);
        } else {
            sendBadRequest(exchange, e.getMessage());
        }
    }
}
package ru.common.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.common.manager.TaskManager;
import ru.common.model.Task;
import ru.common.server.BaseHttpHandler;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public HistoryHandler(TaskManager manager, Gson gson) {
        super(gson);
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendNotFound(exchange);
            return;
        }

        try {
            List<Task> history = manager.getHistory();
            sendSuccess(exchange, toJson(history));
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}
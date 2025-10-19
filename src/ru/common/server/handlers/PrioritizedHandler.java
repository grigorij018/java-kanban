package ru.common.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.common.manager.TaskManager;
import ru.common.model.Task;
import ru.common.server.BaseHttpHandler;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager, Gson gson) {
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
            List<Task> prioritized = manager.getPrioritizedTasks();
            sendSuccess(exchange, toJson(prioritized));
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }
}

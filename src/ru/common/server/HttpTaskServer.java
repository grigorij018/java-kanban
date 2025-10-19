package ru.common.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.common.manager.Managers;
import ru.common.manager.TaskManager;
import ru.common.server.handlers.*;
import ru.common.util.DurationAdapter;
import ru.common.util.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager manager;
    private final Gson gson;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.gson = createGson();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        configureHandlers();
    }

    private Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    private void configureHandlers() {
        server.createContext("/tasks", new TasksHandler(manager, gson));
        server.createContext("/subtasks", new SubtasksHandler(manager, gson));
        server.createContext("/epics", new EpicsHandler(manager, gson));
        server.createContext("/history", new HistoryHandler(manager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(manager, gson));
    }

    public void start() {
        System.out.println("Сервер запущен на порту " + PORT);
        server.start();
    }

    public void stop() {
        System.out.println("Сервер остановлен");
        server.stop(0);
    }

    public static Gson getGson() {
        HttpTaskServer server = null;
        try {
            server = new HttpTaskServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return server.gson;
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();
    }
}
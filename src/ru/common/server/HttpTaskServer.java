package ru.common.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.common.manager.Managers;
import ru.common.manager.TaskManager;
import ru.common.server.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private HttpServer server;
    private final TaskManager manager;
    private final Gson gson;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.gson = createGson();
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
        try {
            if (server != null) {
                stop();
            }
            this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
            configureHandlers();
            server.start();
            System.out.println("Сервер запущен на порту " + PORT);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось запустить сервер на порту " + PORT, e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
            server = null;
            System.out.println("Сервер остановлен");
        }
    }

    public Gson getGson() {
        return gson;
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();

        // Добавляем shutdown hook для graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
import ru.common.manager.TaskManager;
import ru.common.model.Epic;
import ru.common.model.SubTask;
import ru.common.model.Task;
import ru.common.manager.Managers;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создаем задачи
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic1);
        SubTask subTask1 = new SubTask("Подзадача 1", "Описание 1", epic1.getId());
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание 2", epic1.getId());
        SubTask subTask3 = new SubTask("Подзадача 3", "Описание 3", epic1.getId());
        manager.createSubtask(subTask1);
        manager.createSubtask(subTask2);
        manager.createSubtask(subTask3);

        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        manager.createEpic(epic2);

        // Запрашиваем задачи в разном порядке
        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subTask1.getId());
        manager.getTask(task2.getId());
        manager.getEpic(epic1.getId()); // Повторный запрос
        manager.getSubtask(subTask1.getId()); // Повторный запрос

        System.out.println("История после запросов:");
        manager.getHistory().forEach(System.out::println);

        // Удаляем задачу из истории
        manager.deleteTask(task1.getId());
        System.out.println("\nИстория после удаления задачи 1:");
        manager.getHistory().forEach(System.out::println);

        // Удаляем эпик с подзадачами
        manager.deleteEpic(epic1.getId());
        System.out.println("\nИстория после удаления эпика 1:");
        manager.getHistory().forEach(System.out::println);
    }
}
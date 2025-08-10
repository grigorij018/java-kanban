import ru.common.manager.TaskManager;
import ru.common.model.Epic;
import ru.common.model.SubTask;
import ru.common.model.Task;
import ru.common.model.TaskStatus;
import ru.common.manager.Managers;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

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

        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        manager.createEpic(epic2);
        SubTask subTask3 = new SubTask("Подзадача 3", "Описание подзадачи 3", epic2.getId());
        manager.createSubtask(subTask3);

        System.out.println("--- Списки после создания ---");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);

        subTask1.setStatus(TaskStatus.DONE);
        subTask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subTask1);
        manager.updateSubtask(subTask2);

        subTask3.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subTask3);

        System.out.println("\n--- Списки после изменения статусов ---");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());

        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic1.getId());

        System.out.println("\n--- Списки после удаления ---");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubtasks());
    }
}

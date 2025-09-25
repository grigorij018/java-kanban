package ru.common.test;

import org.junit.jupiter.api.Test;
import ru.common.model.Epic;
import ru.common.model.SubTask;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void epicCannotBeItsOwnSubtask() {
        Epic epic = new Epic("Epic", "Description");
        SubTask subTask = new SubTask("Subtask", "Description", epic.getId());
        subTask.setId(epic.getId());

        assertThrows(IllegalArgumentException.class, () -> {
            if (subTask.getId() == subTask.getEpicId()) {
                throw new IllegalArgumentException("Эпик не может быть своей же подзадачей");
            }
        });
    }
}
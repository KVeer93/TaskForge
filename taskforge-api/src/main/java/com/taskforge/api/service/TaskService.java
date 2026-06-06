package com.taskforge.api.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.taskforge.api.dto.CreateTaskRequest;
import com.taskforge.api.entity.Task;
import com.taskforge.api.entity.TaskStatus;
import com.taskforge.api.repository.TaskRepository;
import java.util.List;


@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task createTask(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setType(request.getType());
        task.setPriority(request.getPriority());
        task.setStatus(TaskStatus.QUEUED);
        task.setCreatedAt(LocalDateTime.now());

        return repository.save(task);
    }

    public List<Task> getAllTasks(){
        return repository.findAll();
    }
}

package com.taskforge.api.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.taskforge.api.dto.CreateTaskRequest;
import com.taskforge.api.entity.Task;
import com.taskforge.api.entity.TaskStatus;
import com.taskforge.api.repository.TaskRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


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

    @Transactional
    public Optional<Task> getNextTask(String workerId){
        Optional<Task> taskOptional = repository.findFirstByStatusOrderByPriorityDesc(TaskStatus.QUEUED);

        if(taskOptional.isEmpty()){
            return Optional.empty();
        }

        Task task = taskOptional.get();

        task.setStatus(TaskStatus.PROCESSING);
        task.setStartedAt(LocalDateTime.now());

        task.setAssignedWorker(workerId);
        repository.save(task);

        return Optional.of(task);
    }

    public Task completeTask(Long id){
        Task task = repository.findById(id).orElseThrow();
        task.setStatus(TaskStatus.COMPLETED);
        task.setFinishedAt(LocalDateTime.now());

        return repository.save(task);
    }

    public void deleteAllTasks() {
        repository.deleteAll();
    }

}

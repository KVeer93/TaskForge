package com.taskforge.api.controller;

import com.taskforge.api.entity.Task;
import org.springframework.web.bind.annotation.*;
import com.taskforge.api.service.TaskService;
import com.taskforge.api.dto.CreateTaskRequest;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Task createTask(@RequestBody CreateTaskRequest request){
        return taskService.createTask(request);

    }

    @GetMapping
    public List<Task> getAllTasks(){
        return taskService.getAllTasks();
    }

    @GetMapping("/next")
    public Optional<Task> getNextTask(@RequestParam String workerId){
        return taskService.getNextTask(workerId);
    }

    @PutMapping("/{id}/complete")
    public Task completeTask(@PathVariable Long id){
        return taskService.completeTask(id);
    }

    @DeleteMapping
    public String deleteAllTasks() {
        taskService.deleteAllTasks();
        return "All tasks deleted";
    }

}

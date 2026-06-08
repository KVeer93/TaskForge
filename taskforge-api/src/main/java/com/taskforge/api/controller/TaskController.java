package com.taskforge.api.controller;

import com.taskforge.api.entity.Task;
import org.springframework.web.bind.annotation.*;
import com.taskforge.api.service.TaskService;
import com.taskforge.api.dto.CreateTaskRequest;
import java.util.List;


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
    public Task getNextTask(){
        return taskService.getNextTask().orElse(null);
    }

    @PutMapping("/{id}/complete")
    public Task completeTask(@PathVariable Long id){
        return taskService.completeTask(id);
    }

}

package com.taskforge.api.controller;

import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.*;
import com.taskforge.api.service.TaskService;
import com.taskforge.api.dto.CreateTaskRequest;


@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public String createTask(@RequestBody CreateTaskRequest request){
        return taskService.createTask(request);
    }
}

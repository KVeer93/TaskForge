package com.taskforge.api.service;

import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;
import com.taskforge.api.dto.CreateTaskRequest;

@Service
public class TaskService {

    public String createTask(CreateTaskRequest request) {
        return "Created task: " + request.getTitle();
    }
}

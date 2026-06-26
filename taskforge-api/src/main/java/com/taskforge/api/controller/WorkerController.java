package com.taskforge.api.controller;

import com.taskforge.api.dto.RegisterWorkerRequest;
import com.taskforge.api.entity.Worker;
import com.taskforge.api.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workers")
public class WorkerController {

    private final WorkerService workerService;
    public WorkerController(WorkerService workerService){
        this.workerService = workerService;
    }

    @PostMapping("/register")
    public Void registerWorker(@RequestBody RegisterWorkerRequest request){
        workerService.registerWorker(request.getWorkerId());
        return null;
    }
}

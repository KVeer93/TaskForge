package com.taskforge.api.service;

import com.taskforge.api.entity.Worker;
import com.taskforge.api.entity.WorkerStatus;
import com.taskforge.api.repository.WorkerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WorkerService {
    private final WorkerRepository workerRepository;
    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    public Worker registerWorker(String workerId) {
        Optional<Worker> optionalWorker = workerRepository.findById(workerId);

        if(optionalWorker.isPresent()) {
            Worker worker = optionalWorker.get();
            worker.setStatus(WorkerStatus.ONLINE);
            worker.setLastHeartbeat(LocalDateTime.now());

            return workerRepository.save(worker);
        }

        Worker worker = new Worker();

        worker.setWorkerId(workerId);
        worker.setRegisteredAt(LocalDateTime.now());
        worker.setLastHeartbeat(LocalDateTime.now());
        worker.setStatus(WorkerStatus.ONLINE);

        return workerRepository.save(worker);
    }
}

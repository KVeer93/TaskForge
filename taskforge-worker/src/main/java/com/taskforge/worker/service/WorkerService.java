package com.taskforge.worker.service;

import com.taskforge.worker.dto.RegisterWorkerRequest;
import com.taskforge.worker.dto.TaskResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WorkerService implements CommandLineRunner {

    private final RestTemplate restTemplate;
    @Value("${worker.id}")
    private String workerId;
    public WorkerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private void registerWorker(){
        RegisterWorkerRequest request = new RegisterWorkerRequest();
        request.setWorkerId(workerId);

        restTemplate.postForObject("http://localhost:8080/workers/register", request, Void.class);
    }

    @Override
    public void run(String... args) throws Exception {

        registerWorker();

        while(true){
            System.out.println("Checking for tasks...");


            TaskResponse task =
                    restTemplate.getForObject(
                            "http://localhost:8080/tasks/next?workerId=" + workerId,
                            TaskResponse.class
                    );

            if(task != null){
                System.out.println(
                        "Processing task : " + task.getTitle()
                );

                Thread.sleep(5000);

                restTemplate.put(
                        "http://localhost:8080/tasks/"
                                + task.getId()
                                + "/complete",
                        null
                );

                System.out.println(
                        "Completed task "
                                + task.getId()
                );
            }

            Thread.sleep(5000);
        }
    }
}

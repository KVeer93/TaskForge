package com.taskforge.api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workers")
public class Worker {
    @Id
    private String workerId;

    private LocalDateTime registeredAt;
    private LocalDateTime lastHeartbeat;

    @Enumerated(EnumType.STRING)
    private WorkerStatus status;

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }
    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
    public String getWorkerId() {
        return workerId;
    }

    public void setStatus(WorkerStatus status) {
        this.status = status;
    }
    public WorkerStatus getStatus() {
        return status;
    }

}

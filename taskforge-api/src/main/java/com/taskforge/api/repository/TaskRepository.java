package com.taskforge.api.repository;


import com.taskforge.api.entity.Task;
import com.taskforge.api.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findFirstByStatusOrderByPriorityDesc(TaskStatus status);
    
}
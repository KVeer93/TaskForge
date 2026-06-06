package com.taskforge.api.repository;


import com.taskforge.api.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TaskRepository
        extends JpaRepository<Task, Long> {


}
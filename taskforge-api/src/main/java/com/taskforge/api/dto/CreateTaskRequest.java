package com.taskforge.api.dto;

import org.springframework.web.bind.annotation.GetMapping;

public class CreateTaskRequest {
    private String title;
    private String type;
    private Integer priority;

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }
    public Integer getPriority(){
        return priority;
    }

    public void setPriority(Integer priority){
        this.priority = priority;
    }

}

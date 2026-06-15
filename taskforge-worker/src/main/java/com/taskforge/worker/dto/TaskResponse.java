package com.taskforge.worker.dto;

public class TaskResponse {
    private Long id;
    private String title;
    private String type;
    private String status;
    private Integer priority;

    public Long getId() {
        return id;
    }


    public String getTitle() {
        return title;
    }


    public String getType() {
        return type;
    }


    public String getStatus() {
        return status;
    }


    public Integer getPriority() {
        return priority;
    }



    public void setId(Long id) {
        this.id = id;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public void setType(String type) {
        this.type = type;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public void setPriority(Integer priority) {
        this.priority = priority;
    }

}

package com.group.admin.entity;

import lombok.Data;

@Data
public class News {
    private Long id;
    private String author;
    private String content;
    private java.time.LocalDateTime createdDate;
    private String imageUrls;
    private String newsUid;
    private String preview;
    private String status;
    private String title;
    private java.time.LocalDateTime updatedDate;
    private java.time.LocalDateTime endDate;
    private Boolean isDisplayOnHome;
    private java.time.LocalDateTime startDate;
}

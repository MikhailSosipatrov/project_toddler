package com.toddler.dto;

import lombok.Data;

@Data
public class CommentDto {
    private String userName; // From users.username
    private String content;
    private String createdAt; // ISO date string
}

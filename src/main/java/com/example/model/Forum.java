package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Forum {
    private String id;
    private String title;
    private String creator;
    private String createdAt;
    private long visits;
}
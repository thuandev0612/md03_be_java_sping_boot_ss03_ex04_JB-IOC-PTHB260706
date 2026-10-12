package com.example.baitapsession03.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    private Long id;
    private String title;
    private String status;
    private Long instructorId;
}

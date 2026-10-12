package com.example.baitapsession03.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    private Long id;
    private String studentName;
    private Long courseId;
}

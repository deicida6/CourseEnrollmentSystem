package com.example.ces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String name;
    private int occupiedSpots;
    private int availableSpots;
}

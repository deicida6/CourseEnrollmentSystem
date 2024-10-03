package com.example.ces.controller;

import com.example.ces.dto.CourseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.ces.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // Эндпоинт для записи студента на курс
    @PostMapping("/enroll")
    public String enrollStudent(@RequestParam Long studentId, @RequestParam Long courseId) {
        return courseService.enrollStudent(studentId, courseId);
    }

    // Эндпоинт для получения списка курсов с количеством свободных и занятых мест
    @GetMapping("/status")
    public List<CourseDto> getAllCourses() {
        return courseService.getAllCoursesWithAvailableSpots();
    }

    // Эндпоинт для получения информации о конкретном курсе по его ID с количеством свободных и занятых мест
    @GetMapping("/{courseId}")
    public CourseDto getCourseById(@PathVariable Long courseId) {
        return courseService.getCourseById(courseId);
    }
}

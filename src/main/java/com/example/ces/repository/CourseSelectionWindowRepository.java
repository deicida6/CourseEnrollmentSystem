package com.example.ces.repository;

import com.example.ces.model.Course;
import com.example.ces.model.CourseSelectionWindow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseSelectionWindowRepository extends JpaRepository<CourseSelectionWindow, Long> {
    CourseSelectionWindow findByCourse(Course course);
}
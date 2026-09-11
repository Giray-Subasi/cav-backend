package com.example.cav_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from CAV Backend";
    }

    @GetMapping("/status")
    public String status() {
        return "CAV Backend is running";
    }
}
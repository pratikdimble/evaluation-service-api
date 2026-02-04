package org.example.demo.controller;

import org.example.demo.model.ModelDTO;
import org.example.demo.service.IEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EvaluationController {
    @Autowired
    IEvaluationService evaluationService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot (no Initializr)";
    }

    @GetMapping("/evaluate")
    public ResponseEntity<String> readCSV()
    {
        return ResponseEntity.ok(evaluationService.readCSV());
    }

    @GetMapping("/evaluate/bulk")
    public ResponseEntity<List<ModelDTO>> readMultipleCSVs()
    {
        return ResponseEntity.ok(evaluationService.readMultipleCSVs());
    }
}



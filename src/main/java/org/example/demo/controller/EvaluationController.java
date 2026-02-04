package org.example.demo.controller;

import org.example.demo.model.ModelDTO;
import org.example.demo.model.ResultDTO;
import org.example.demo.service.IEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/evaluate/csv")
    public ResponseEntity<String> readCSV()
    {
        return ResponseEntity.ok(evaluationService.readCSV());
    }

    @GetMapping("/evaluate/bulk")
    public ResponseEntity<List<ModelDTO>> readMultipleCSVs()
    {
        return ResponseEntity.ok(evaluationService.readMultipleCSVs());
    }

    @GetMapping("/evaluate/online")
    public ResponseEntity<ResultDTO> evaluateOnlineCSVs()
    {
        return ResponseEntity.ok(evaluationService.readResourcesCSVs(false));
    }

    @GetMapping("/evaluate/batch")
    public ResponseEntity<ResultDTO> evaluateBatchCSVs()
    {
        return ResponseEntity.ok(evaluationService.readResourcesCSVs(true));
    }

    @GetMapping("/evaluate/exportCsv")
    public String exportCsv(@RequestParam(defaultValue = "false") boolean isBatch) {
        evaluationService.exportCsv(isBatch);
        return "CSV export triggered. Check "+"result_summary"+(isBatch ? "_batch" : "_online")+".csv"+" in your application directory.";
    }
}



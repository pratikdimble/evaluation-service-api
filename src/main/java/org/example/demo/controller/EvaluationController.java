package org.example.demo.controller;

import org.example.demo.model.ModelDTO;
import org.example.demo.model.ResultDTO;
import org.example.demo.service.IEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/evaluate")
@Tag(name = "Evaluation APIs", description = "Endpoints for evaluating and exporting CSVs")
public class EvaluationController {

    @Autowired
    IEvaluationService evaluationService;

    @Operation(summary = "Simple hello endpoint", description = "Returns a hello message")
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot (no Initializr)";
    }

   /* @Operation(summary = "Read single CSV", description = "Reads a single CSV file and returns its content")
    @GetMapping("/csv")
    public ResponseEntity<String> readCSV() {
        return ResponseEntity.ok(evaluationService.readCSV());
    }

    @Operation(summary = "Read multiple CSVs", description = "Reads multiple CSV files and returns a list of models")
    @GetMapping("/bulk")
    public ResponseEntity<List<ModelDTO>> readMultipleCSVs() {
        return ResponseEntity.ok(evaluationService.readMultipleCSVs());
    }*/

    @Operation(summary = "Evaluate Online CSVs", description = "Evaluates CSVs from online resources")
    @GetMapping("/online")
    public ResponseEntity<ResultDTO> evaluateOnlineCSVs() {
        return ResponseEntity.ok(evaluationService.readResourcesCSVs(false));
    }

    @Operation(summary = "Evaluate Batch CSVs", description = "Evaluates CSVs from batch resources")
    @GetMapping("/batch")
    public ResponseEntity<ResultDTO> evaluateBatchCSVs() {
        return ResponseEntity.ok(evaluationService.readResourcesCSVs(true));
    }

    @Operation(summary = "Export results to CSV", description = "Exports evaluation results into a CSV file")
    @GetMapping("/exportCsv")
    public String exportCsv(@RequestParam(defaultValue = "false") boolean isBatch) {
        evaluationService.exportCsv(isBatch);
        return "CSV export triggered. Check " +
                "result_summary" + (isBatch ? "_batch" : "_online") + ".csv" +
                " in your application directory.";
    }
}

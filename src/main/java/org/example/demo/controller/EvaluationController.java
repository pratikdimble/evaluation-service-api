package org.example.demo.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.example.demo.model.ModelDTO;
import org.example.demo.model.OutputDTO;
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
    @GetMapping("/export-csv")
    public String exportCsv(@RequestParam(defaultValue = "false") boolean isBatch) {
        evaluationService.exportCsv(isBatch);
        return "CSV export triggered. Check " +
                "result_summary" + (isBatch ? "_batch" : "_online") + ".csv" +
                " in your application directory.";
    }

    @Operation(
            summary = "Fetch model details",
            description = "Fetches detailed OutputDTO records for a specific model directory. " +
                    "Pass the model name (directory) and whether to use Batch or Online mode."
    )
    @GetMapping("/model-detail/{model}")
    public ResponseEntity<List<OutputDTO>> fetchModelDetail(
            @Parameter(description = "Model directory name, e.g. APRI03")
            @PathVariable String model,
            @Parameter(description = "Set true for Batch mode, false for Online mode")
            @RequestParam(defaultValue = "false") boolean isBatch) {

        List<OutputDTO> details = evaluationService.fetchModelDetail(model, isBatch);
        return ResponseEntity.ok(details);
    }

    @Operation(
            summary = "Export all models summary CSV",
            description = "Exports evaluation results for all models into a summary CSV file. " +
                    "Set isBatch=true for Batch mode, false for Online mode."
    )
    @GetMapping("/export-model-csv")
    public ResponseEntity<String> exportModelCsv(
            @Parameter(description = "Set true for Batch mode, false for Online mode")
            @RequestParam(defaultValue = "false") boolean isBatch) {

        evaluationService.exportModelCsv(isBatch);
        return ResponseEntity.ok("Model summary CSV export triggered for " +
                (isBatch ? "Batch" : "Online") + " mode.");
    }

    @Operation(
            summary = "Export detail CSV for a specific model",
            description = "Exports detailed evaluation results for a given model into a CSV file. " +
                    "Provide the model name as a path variable and set isBatch accordingly."
    )
    @GetMapping("/export-detail-csv/{model}")
    public ResponseEntity<String> exportDetailCsv(
            @Parameter(description = "Model directory name, e.g. APRI03")
            @PathVariable String model,

            @Parameter(description = "Set true for Batch mode, false for Online mode")
            @RequestParam(defaultValue = "false") boolean isBatch) {

        evaluationService.exportDetailCsv(model, isBatch);
        return ResponseEntity.ok("Detail CSV export triggered for model " + model +
                " in " + (isBatch ? "Batch" : "Online") + " mode.");
    }
}

package org.app.eaas.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.app.eaas.model.OutputDTO;
import org.app.eaas.model.ResultDTO;
import org.app.eaas.service.IEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/evaluate")
@Tag(name = "Evaluation APIs", description = "Endpoints for evaluating and exporting CSVs")
public class EvaluationController {

    @Autowired
    IEvaluationService evaluationService;

    @Operation(summary = "Evaluate Online or Batch CSVs",
            description = "Evaluates CSVs from online/batch resources. " +
            "Set isBatch=true for Batch mode, false for Online mode.")
    @GetMapping()
    public ResponseEntity<ResultDTO> evaluateOnlineCSVs(@RequestParam(defaultValue = "false") boolean isBatch) throws IOException {
        return ResponseEntity.ok(evaluationService.readResourcesCSVs(isBatch));
    }

    @Operation(summary = "Evaluate fixed path Online or Batch CSVs",
            description = "Evaluates CSVs from online/batch resources. " +
                    "Set isBatch=true for Batch mode, false for Online mode.")
    @GetMapping("/internal")
    public ResponseEntity<ResultDTO> readMultipleCSVs(@RequestParam(defaultValue = "false") boolean isBatch) throws IOException {
        return ResponseEntity.ok(evaluationService.readMultipleCSVs(isBatch));
    }

    @Operation(summary = "Export model with details results to CSV", description = "Exports evaluation results into a CSV file")
    @GetMapping("/export-csv")
    public String exportCsv(@RequestParam(defaultValue = "false") boolean isBatch) throws IOException {
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
    @GetMapping("/export")
    public ResponseEntity<String> exportModelCsv(
            @Parameter(description = "Set true for Batch mode, false for Online mode")
            @RequestParam(defaultValue = "false") boolean isBatch) throws IOException {

        evaluationService.exportCsv(isBatch);
        return ResponseEntity.ok("Model summary CSV export triggered for " +
                (isBatch ? "Batch" : "Online") + " mode.");
    }

    @Operation(
            summary = "Export detail CSV for a specific model",
            description = "Exports detailed evaluation results for a given model into a CSV file. " +
                    "Provide the model name as a path variable and set isBatch accordingly."
    )
    @GetMapping("/export/{model}")
    public ResponseEntity<String> exportDetailCsv(
            @Parameter(description = "Model directory name, e.g. APRI03")
            @PathVariable String model,

            @Parameter(description = "Set true for Batch mode, false for Online mode")
            @RequestParam(defaultValue = "false") boolean isBatch) {


        return ResponseEntity.ok(evaluationService.exportDetailCsv(model, isBatch));
    }

}

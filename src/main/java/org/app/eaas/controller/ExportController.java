package org.app.eaas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.app.eaas.service.IEvaluationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/export")
public class ExportController {

    private final IEvaluationService evaluationService;

    public ExportController(IEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @Operation(
            summary = "Export all models summary CSV",
            description = "Exports evaluation results for all models into a summary CSV file. " +
                    "Set isBatch=true for Batch mode, false for Online mode."
    )
    @GetMapping("/summary")
    public ResponseEntity<byte[]> downloadSummaryCsv(@RequestParam (defaultValue = "false") Boolean isBatch) throws IOException {
        // Generate CSV content
        String csv = evaluationService.generateCsvString(null, isBatch);

        byte[] data = csv.getBytes(StandardCharsets.UTF_8);

        String filename = "result_summary_" + (isBatch ? "batch" : "online") + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    @Operation(
            summary = "Export detail CSV for a specific model",
            description = "Exports detailed evaluation results for a given model into a CSV file. " +
                    "Provide the model name as a path variable and set isBatch accordingly."
    )
    @GetMapping("/detail/{model}")
    public ResponseEntity<byte[]> downloadDetailCsv(
            @Parameter(description = "Model directory name, e.g. APRI03")
            @PathVariable String model,

            @Parameter(description = "Set true for Batch mode, false for Online mode")
            @RequestParam(defaultValue = "false") boolean isBatch) throws IOException {

        // Generate CSV content
        String csv = evaluationService.generateCsvString(model, isBatch);

        byte[] data = csv.getBytes(StandardCharsets.UTF_8);

        String filename = "result_summary_" + (isBatch ? "batch" : "online") + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);

    }
}


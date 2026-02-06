package org.example.demo.service;


import org.example.demo.model.ModelDTO;
import org.example.demo.model.OutputDTO;
import org.example.demo.model.ResultDTO;

import java.io.IOException;
import java.util.List;

public interface IEvaluationService {

    String readCSV();

    List<ModelDTO> readMultipleCSVs();

    ResultDTO readResourcesCSVs(Boolean isBatch) throws IOException;

    void exportCsv(Boolean isBatch) throws IOException;

    List<OutputDTO> fetchModelDetail(String model, Boolean isBatch);

    void exportModelCsv(Boolean isBatch) throws IOException;

    void exportDetailCsv(String model, Boolean isBatch);
}

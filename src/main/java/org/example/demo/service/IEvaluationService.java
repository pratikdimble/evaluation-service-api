package org.example.demo.service;


import org.example.demo.model.ModelDTO;
import org.example.demo.model.OutputDTO;
import org.example.demo.model.ResultDTO;

import java.util.List;

public interface IEvaluationService {

    String readCSV();

    List<ModelDTO> readMultipleCSVs();

    ResultDTO readResourcesCSVs(Boolean isBatch);

    void exportCsv(Boolean isBatch);

    List<OutputDTO> fetchModelDetail(String model, Boolean isBatch);

    void exportModelCsv(Boolean isBatch);

    void exportDetailCsv(String model, Boolean isBatch);
}

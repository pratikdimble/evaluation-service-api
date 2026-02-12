package org.app.eaas.service;


import org.app.eaas.model.ModelDTO;
import org.app.eaas.model.OutputDTO;
import org.app.eaas.model.ResultDTO;

import java.io.IOException;
import java.util.List;

public interface IEvaluationService {

    String readCSV();

    ResultDTO readMultipleCSVs(Boolean isBatch);

    ResultDTO readResourcesCSVs(Boolean isBatch) throws IOException;

    void exportCsv(Boolean isBatch) throws IOException;

    List<OutputDTO> fetchModelDetail(String model, Boolean isBatch);


    String generateCsvString(String model, Boolean isBatch) throws IOException;

}

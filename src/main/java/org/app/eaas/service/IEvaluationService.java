package org.app.eaas.service;


import org.app.eaas.model.ModelDTO;
import org.app.eaas.model.OutputDTO;
import org.app.eaas.model.ResultDTO;

import java.io.IOException;
import java.util.List;

public interface IEvaluationService {

    String readCSV();

    List<ModelDTO> readMultipleCSVs();

    ResultDTO readResourcesCSVs(Boolean isBatch) throws IOException;

    void exportCsv(Boolean isBatch) throws IOException;

    List<OutputDTO> fetchModelDetail(String model, Boolean isBatch);

    void exportModelCsv(Boolean isBatch) throws IOException;

    String  exportDetailCsv(String model, Boolean isBatch);

}

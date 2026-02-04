package org.example.demo.service;


import org.example.demo.model.ModelDTO;

import java.util.List;

public interface IEvaluationService {

    String readCSV();

    List<ModelDTO> readMultipleCSVs();

    List<ModelDTO> readResourcesCSVs();
}

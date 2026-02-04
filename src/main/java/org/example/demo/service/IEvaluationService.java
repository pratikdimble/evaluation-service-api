package org.example.demo.service;


import org.example.demo.model.ModelDTO;
import org.example.demo.model.ResultDTO;

import java.util.List;

public interface IEvaluationService {

    String readCSV();

    List<ModelDTO> readMultipleCSVs();

    ResultDTO readResourcesCSVs();
}

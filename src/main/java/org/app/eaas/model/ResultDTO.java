package org.app.eaas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResultDTO {
    private Long totalModels;
    private Long diffModels;
    private List<ModelDTO> modelDTOs;
}

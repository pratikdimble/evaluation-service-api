package org.app.eaas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OutputDTO {

    private String inputA;
    private Long inputARecords;
    private String inputB;
    private Long inputBRecords;
    private Long attributesWithDifferences;


}

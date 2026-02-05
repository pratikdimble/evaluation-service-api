package org.example.demo.service;

import org.example.demo.model.ResultDTO;
import org.example.demo.model.OutputDTO;
import org.example.demo.model.ModelDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class EvaluationServiceImpl implements IEvaluationService {

    @Value("${csv.base.online}")
    private String onlineBase;
    @Value("${csv.base.batch}")
    private String batchBase;
    @Value("${csv.fixed.online}")
    private String onlineFixed;
    @Value("${csv.fixed.batch}")
    private String batchFixed;
    @Value("${csv.subpath}")
    private String subPath;

    @Override
    public String readCSV() {
        Path path = Path.of("D:\\testplan_dev\\report\\comparison_summary.csv");
        System.out.println("\n\n\t\t*********** Differences found for below Comparision model  ****************** ");
        try (Stream<String> lines = Files.lines(path)) {
            lines.skip(1) // skip header row
                    .map(line -> line.split(",")) // split by comma
                    .filter(parts -> Integer.parseInt(parts[parts.length - 1]) > 0) // only rows with differences > 0
                    .forEach(parts -> System.out.println("\t\t" + parts[1] + " | " + parts[4] + " | " + parts[parts.length-1])); // print InputA and InputB with Attributes with Differences
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\t\t************ EOF *********** EOF  ************ EOF ************** EOF ******* \n");

        return "OK";
    }

    @Override
    public List<ModelDTO>  readMultipleCSVs() {
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger diffCount = new AtomicInteger(0);
        List<ModelDTO> modelDTOS = new ArrayList<>();
        // Base path where subdirectories exist
        Path basePath = Path.of("D:\\Online");

        // Fixed relative path after each subdirectory
        String fixedPath = "GCP_vsOnPrem\\testplan_dev\\report\\comparison_summary.csv";
//        System.out.println("\n\n\t\t*********** Differences found for below Comparision model  ****************** ");
        try (Stream<Path> dirs = Files.list(basePath)) {
            dirs.filter(Files::isDirectory) //
                    .forEach(dir -> {
                        Path csvPath = dir.resolve(fixedPath); // build full path
                        if (Files.exists(csvPath)) {
                            String resolvedDir = dir.getFileName().toString();
//                            System.out.println("Processing Model: " + resolvedDir);
                            AtomicBoolean foundDiff = new AtomicBoolean(false);
                            List<OutputDTO> attributesList = new ArrayList<>();

                            try (Stream<String> lines = Files.lines(csvPath)) {
                                ModelDTO modelDTO = new ModelDTO();
                                modelDTO.setModel(resolvedDir);
                                lines.skip(1) // skip header
                                        .map(line -> line.split(","))
                                        .filter(parts -> {
                                            int inputARecords = Integer.parseInt(parts[3]);
                                            int inputBRecords = Integer.parseInt(parts[6]);
                                            int differences = Integer.parseInt(parts[parts.length - 1]);
                                            return inputARecords != inputBRecords || differences > 0;
                                        })
                                        //  .filter(parts -> Integer.parseInt(parts[parts.length - 1]) > 0)
                                        .forEach(parts -> {
                                            attributesList.add(new OutputDTO(parts[1], Long.valueOf(parts[3]), parts[4],Long.valueOf(parts[6]), Long.valueOf(parts[parts.length - 1])));
                                            foundDiff.set(true);
//                                            System.out.println("\t\tInputA: " + parts[1] + " | InputB: " + parts[4] + "\n");
                                        });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (!attributesList.isEmpty()) {
                                modelDTOS.add(new ModelDTO(resolvedDir, attributesList));
                            }
                            if (foundDiff.get()) {
                                diffCount.incrementAndGet();
                            }
                            processedCount.incrementAndGet();
                        } else {
                            System.out.println("File not found: " + csvPath);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*System.out.println("\n\t\t************ EOF *********** EOF  ************ EOF ************** EOF ******* \n");
        System.out.println("\nTotal directories processed: " + processedCount.get());
        System.out.println("Directories with differences: " + diffCount.get());*/
        return modelDTOS;
    }

    @Override
    public ResultDTO readResourcesCSVs(Boolean isBatch) {
        List<ModelDTO> modelDTOS = new ArrayList<>();
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger diffCount = new AtomicInteger(0);
        // Base path where subdirectories exist
//        Path basePath = isBatch? Path.of("src/main/resources/Batch") : Path.of("src/main/resources/Online");
        Path basePath = this.resolveBasePath(isBatch);

        // Fixed relative path after each subdirectory
//        String fixedPath = (isBatch? "GCP_Online" : "GCP_vsOnPrem") +"\\testplan_dev\\report\\comparison_summary.csv";
        String fixedPath = this.resolveFixedPath(isBatch);

        try (Stream<Path> dirs = Files.list(basePath)) {
            // Collect all subdirectories dynamically
            List<Path> subDirs = dirs.filter(Files::isDirectory).toList();
            for (Path dir : subDirs) {
                Path csvPath = dir.resolve(fixedPath); // build full path
                if (Files.exists(csvPath)) {
                    String resolvedDir = dir.getFileName().toString();
//                            System.out.println("Processing Model: " + resolvedDir);
                    AtomicBoolean foundDiff = new AtomicBoolean(false);
                    List<OutputDTO> attributesList = new ArrayList<>();

                    try (Stream<String> lines = Files.lines(csvPath)) {
                        ModelDTO modelDTO = new ModelDTO();
                        modelDTO.setModel(resolvedDir);
                        lines.skip(1) // skip header
                                .map(line -> line.split(","))
                                .filter(parts -> {
                                    int inputARecords = Integer.parseInt(parts[3]);
                                    int inputBRecords = Integer.parseInt(parts[6]);
                                    int differences = Integer.parseInt(parts[parts.length - 1]);
                                    return inputARecords != inputBRecords || differences > 0;
                                })
                                .forEach(parts -> {
                                    attributesList.add(new OutputDTO(parts[1],
                                            Long.valueOf(parts[3]), parts[4],
                                            Long.valueOf(parts[6]),
                                            Long.valueOf(parts[parts.length - 1]))
                                    );
                                    foundDiff.set(true);
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!attributesList.isEmpty()) {
                        modelDTOS.add(new ModelDTO(resolvedDir, attributesList));
                    }
                    if (foundDiff.get()) {
                        diffCount.incrementAndGet();
                    }
                    processedCount.incrementAndGet();
                } else {
                    System.out.println("File not found: " + csvPath);
                }
                    };
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\n\t\t Total directories processed: " + processedCount.get());
        System.out.println("\t\t Directories with differences: " + diffCount.get());
        return new ResultDTO((long) processedCount.get(), (long) diffCount.get(), modelDTOS);
    }

    @Override
    public void exportCsv(Boolean isBatch) {
        ResultDTO  resultDTO = this.readResourcesCSVs(isBatch);
        if(resultDTO != null){
            if(!resultDTO.getModelDTOs().isEmpty()){
                List<ModelDTO> modelDTOs = new ArrayList<>(resultDTO.getModelDTOs());
                // Export to CSV
                String[] header = {"Model", "InputA", "InputA Records", "InputB", "InputB Records", "Attributes With Differences"};
                try (FileWriter writer = new FileWriter("result_summary"+(isBatch ? "_batch" : "_online")+".csv")) {
                    // Write header
                    writer.append(String.join(",", header)).append("\n");

                    // Write rows
                    for (ModelDTO model : modelDTOs) {
                        for (OutputDTO dto : model.getOutputDTOList()) {
                            writer.append(model.getModel()).append(",")
                                    .append(String.valueOf(dto.getInputA())).append(",")
                                    .append(String.valueOf(dto.getInputARecords())).append(",")
                                    .append(String.valueOf(dto.getInputB())).append(",")
                                    .append(String.valueOf(dto.getInputBRecords())).append(",")
                                    .append(String.valueOf(dto.getAttributesWithDifferences()))
                                    .append("\n");
                        }
                    }
                    System.out.println("CSV file created: "+"result_summary"+(isBatch ? "_batch" : "_online")+".csv");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /*********************************
     * HELPER METHODS STARTS FROM HERE
     * *******************************/
    private Path resolveBasePath(boolean isBatch) {
        String base = isBatch ? batchBase : onlineBase;
        return Path.of(base);
    }

    private String resolveFixedPath(boolean isBatch) {
        String fixed = isBatch ? batchFixed : onlineFixed;
        return Paths.get(fixed, subPath.split("/")).toString();
    }
}

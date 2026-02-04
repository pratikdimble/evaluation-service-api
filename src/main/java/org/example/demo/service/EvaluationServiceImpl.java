package org.example.demo.service;

import org.example.demo.model.outputDTO;
import org.example.demo.model.ModelDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class EvaluationServiceImpl implements IEvaluationService {

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
        // Base path where subdirectories like CDIE03, CDIE04, CDIE05 exist
        Path basePath = Path.of("D:\\Online");

        List<ModelDTO> modelDTOS = new ArrayList<>();

        // Fixed relative path after each subdirectory
        String fixedPath = "GCP_vsOnPrem\\testplan_dev\\report\\comparison_summary.csv";
//        System.out.println("\n\n\t\t*********** Differences found for below Comparision model  ****************** ");
        try (Stream<Path> dirs = Files.list(basePath)) {
            dirs.filter(Files::isDirectory) // only directories like CDIE03, CDIE04...
                    .forEach(dir -> {
                        Path csvPath = dir.resolve(fixedPath); // build full path
                        if (Files.exists(csvPath)) {
                            String resolvedDir = dir.getFileName().toString(); // just CDIE03, CDIE04, etc.
//                            System.out.println("Processing Model: " + resolvedDir);
                            AtomicBoolean foundDiff = new AtomicBoolean(false);
                            List<outputDTO> attributesList = new ArrayList<>();

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
                                            attributesList.add(new outputDTO(parts[1], parts[4]));
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
}

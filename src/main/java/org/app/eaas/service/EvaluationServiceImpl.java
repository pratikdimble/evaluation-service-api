package org.app.eaas.service;

import org.app.eaas.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EvaluationServiceImpl implements IEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationServiceImpl.class);

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

    @Value("${csv.prefix.online}")
    private String onlinePrefix;

    @Value("${csv.prefix.batch}")
    private String batchPrefix;

    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    @Override
    public String readCSV() {
        Path path = Path.of("D:\\testplan_dev\\report\\comparison_summary.csv");
        log.info("Reading CSV differences from {}", path);

        try (Stream<String> lines = Files.lines(path)) {
            lines.skip(1)
                    .map(line -> line.split(","))
                    .filter(parts -> safeParseInt(parts[parts.length - 1]) > 0)
                    .forEach(parts -> log.info("Model: {} | {} | {}", parts[1], parts[4], parts[parts.length - 1]));
        } catch (IOException e) {
            log.error("Error reading CSV", e);
        }

        return "OK";
    }

    @Override
    public ResultDTO readMultipleCSVs(Boolean isBatch) {
        Path basePath = resolveBasePath(isBatch, true);
        Path fixedRelative = resolveFixedPath(isBatch);

        List<Future<ModelDTO>> tasks = new ArrayList<>();

        ExecutorService executor = createCsvProcessingExecutor(getDirCount(basePath));

        try (Stream<Path> dirs = Files.list(basePath)) {
            dirs.filter(Files::isDirectory)
                    .forEach(dir -> tasks.add(executor.submit(() -> processDirectory(dir, fixedRelative))));
            printThreadLogs(executor);
        } catch (IOException e) {
            log.error("Error listing base directory {}", basePath, e);
        }

        List<ModelDTO> models = collectResults(tasks);
        executor.shutdown();

        long processed = models.size();
        long diffCount = models.stream().filter(m -> !m.getOutputDTOList().isEmpty()).count();

        log.info("Processed: {} directories, Differences found in {} directories", processed, diffCount);
        return new ResultDTO(processed, diffCount, models);
    }

    private ModelDTO processDirectory(Path dir, Path fixedPath) {
        Path csv = dir.resolve(fixedPath);
        if (!Files.exists(csv)) {
            log.warn("CSV not found in {}", dir);
            return null;
        }

        List<OutputDTO> outputs = new ArrayList<>();
        try (Stream<String> lines = Files.lines(csv)) {
            parseCsv(lines, outputs);
        } catch (IOException e) {
            log.error("Error reading CSV {}", csv, e);
        }

        return new ModelDTO(dir.getFileName().toString(), outputs, new Date(csv.toFile().lastModified()));
    }

    @Override
    public ResultDTO readResourcesCSVs(Boolean isBatch) throws IOException {
        List<ModelDTO> models = new Vector<>();
        AtomicInteger processedCount = new AtomicInteger();
        AtomicInteger diffCount = new AtomicInteger();

        List<String> resourcePaths = readManifest(isBatch);

        ExecutorService executor = createCsvProcessingExecutor(resourcePaths.size());

        List<Future<Void>> tasks = new ArrayList<>();

        for (String resourcePath : resourcePaths) {
            tasks.add(executor.submit(() -> {
                loadClassPathCsv(resourcePath, models, processedCount, diffCount);
                return null;
            }));
        }
        printThreadLogs(executor);
        waitForTasks(tasks, executor);

        log.info("Read {} resources, {} with diffs", processedCount.get(), diffCount.get());
        return new ResultDTO((long)processedCount.get(), (long)diffCount.get(), models);
    }

    private void loadClassPathCsv(String resourcePath,
                                  List<ModelDTO> models,
                                  AtomicInteger processedCount,
                                  AtomicInteger diffCount) {
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            List<OutputDTO> outputs = new ArrayList<>();
            parseCsv(reader.lines(), outputs);

            String modelName = extractModelName(resourcePath);
            models.add(new ModelDTO(modelName, outputs, new Date()));
            processedCount.incrementAndGet();
            if (!outputs.isEmpty()) {
                diffCount.incrementAndGet();
            }

        } catch (IOException e) {
            log.error("Resource {} not found", resourcePath, e);
        }
    }

    private List<String> readManifest(Boolean isBatch) throws IOException {
        ClassPathResource manifest = new ClassPathResource(isBatch ? "batch.txt" : "online.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(manifest.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    @Override
    public void exportCsv(Boolean isBatch) throws IOException {
        ResultDTO result = readResourcesCSVs(isBatch);
        writeCsv(result.getModelDTOs(), isBatch ? "batch" : "online", false);
    }

    private void writeCsv(List<ModelDTO> models, String type, boolean detail) throws IOException {
        String filename = detail
                ? type + "_detail_summary.csv"
                : type + "_summary.csv";

        log.info("Exporting CSV to {}", filename);

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filename))) {
            writer.write("Model,InputA,InputA Records,InputB,InputB Records,Attributes With Differences");
            writer.newLine();

            for (ModelDTO m : models) {
                for (OutputDTO o : m.getOutputDTOList()) {
                    writer.write(String.join(",",
                            m.getModel(),
                            o.getInputA(),
                            String.valueOf(o.getInputARecords()),
                            o.getInputB(),
                            String.valueOf(o.getInputBRecords()),
                            String.valueOf(o.getAttributesWithDifferences())));
                    writer.newLine();
                }
            }
        }
    }

    @Override
    public List<OutputDTO> fetchModelDetail(String model, Boolean isBatch) {
        try {
            return readResourcesCSVs(isBatch).getModelDTOs().stream()
                    .filter(m -> model.equals(m.getModel()))
                    .map(ModelDTO::getOutputDTOList)
                    .findFirst()
                    .orElse(Collections.emptyList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String exportDetailCsv(String model, Boolean isBatch) {
        List<OutputDTO> outputs = fetchModelDetail(model, isBatch);
        if (outputs.isEmpty()) {
            return "No data found for model: " + model;
        }
        try {
            writeCsv(Collections.singletonList(new ModelDTO(model, outputs, new Date())), model, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "Detail CSV exported for " + model;
    }

    @Override
    public String generateCsvString(String model, Boolean isBatch) throws IOException {
        List<ModelDTO> models = new ArrayList<>();
        if(model == null)
            models = readResourcesCSVs(isBatch).getModelDTOs();
        else{
            List<OutputDTO> outputs = fetchModelDetail(model, isBatch);
            models.add(new ModelDTO(model, outputs, new Date()));
            }
        StringBuilder sb = new StringBuilder();
        sb.append("Model,InputA,InputA Records,InputB,InputB Records,Attributes With Differences\n");

        for (ModelDTO m : models) {
            for (OutputDTO o : m.getOutputDTOList()) {
                sb.append(m.getModel()).append(",")
                        .append(o.getInputA()).append(",")
                        .append(o.getInputARecords()).append(",")
                        .append(o.getInputB()).append(",")
                        .append(o.getInputBRecords()).append(",")
                        .append(o.getAttributesWithDifferences()).append("\n");
            }
        }
        return sb.toString();
    }


    /* =======================
       ===== Helper Methods ======
       ======================= */

    private ExecutorService createCsvProcessingExecutor(int maxTasks) {
        int cores = Runtime.getRuntime().availableProcessors();

        // Core threads for CPU work, max threads higher for I/O waits
        int maxPoolSize = cores * 2 + 1;
        long keepAlive = 60L;

        // Bounded queue prevents unbounded memory growth
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(Math.max(maxTasks, 100));

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                cores,
                maxPoolSize,
                keepAlive,
                TimeUnit.SECONDS,
                queue,
                new ThreadPoolExecutor.CallerRunsPolicy() // slows down submission when saturated
        );

        executor.allowCoreThreadTimeOut(true); // let idle threads exit

        return executor;
    }

    private void parseCsv(Stream<String> lines, List<OutputDTO> outputs) {
        lines.skip(1)
                .map(line -> line.split(","))
                .filter(this::hasDifference)
                .map(parts -> new OutputDTO(
                        parts[1],
                        safeParseLong(parts[3]),
                        parts[4],
                        safeParseLong(parts[6]),
                        safeParseLong(parts[parts.length - 1])
                ))
                .forEach(outputs::add);
    }

    private boolean hasDifference(String[] parts) {
        return safeParseLong(parts[3]) != safeParseLong(parts[6])
                || safeParseLong(parts[parts.length - 1]) > 0;
    }

    private int safeParseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) {
            log.warn("Invalid integer format: {}", s);
            return 0;
        }
    }

    private long safeParseLong(String s) {
        try { return Long.parseLong(s.trim()); }
        catch (NumberFormatException e) {
            log.warn("Invalid long format: {}", s);
            return 0L;
        }
    }

    private Path resolveBasePath(boolean isBatch, boolean isInternal) {
        String base = isBatch
                ? (isInternal ? batchPrefix : batchBase)
                : (isInternal ? onlinePrefix : onlineBase);
        return Paths.get(base);
    }

    private Path resolveFixedPath(boolean isBatch) {
        String fixed = isBatch ? batchFixed : onlineFixed;
        return Paths.get(fixed, subPath.split("/"));
    }

    private void printThreadLogs(ExecutorService executor){
        ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
        log.info("Active: {} Queue: {} Completed: {}",
                tpe.getActiveCount(),
                tpe.getQueue().size(),
                tpe.getCompletedTaskCount());
    }

    private int getDirCount(Path basePath){
        Long dirCount = 0L;
        try (Stream<Path> dirs = Files.list(basePath)) {
            dirCount = dirs.filter(Files::isDirectory).count();
        } catch (IOException e) {
            log.error("Error listing base path {}", basePath, e);
        }
        return dirCount.intValue();
    }

    private String extractModelName(String csvPath) {
        // Normalize separators
        String[] parts = csvPath.split("/");

        for (int i = 0; i < parts.length; i++) {
            if ("GCP_Online".equals(parts[i]) || "GCP_vsOnPrem".equals(parts[i])) {
                // Model name is the segment just before this
                if (i > 0) {
                    return parts[i - 1];
                }
            }
        }
        return null; // not found
    }

    private <T> List<T> collectResults(List<Future<T>> futures) {
        List<T> results = new ArrayList<>();
        for (Future<T> f : futures) {
            try {
                T r = f.get();
                if (r != null) results.add(r);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error collecting future", e);
            }
        }
        return results;
    }

    private void waitForTasks(List<Future<Void>> futures, ExecutorService executor) {
        futures.forEach(f -> {
            try { f.get(); }
            catch (InterruptedException | ExecutionException e) { log.error("Error", e); }
        });
        executor.shutdown();
    }
}

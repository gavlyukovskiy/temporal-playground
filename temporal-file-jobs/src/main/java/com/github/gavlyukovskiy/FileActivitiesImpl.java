package com.github.gavlyukovskiy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class FileActivitiesImpl implements FileActivities {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String workerId;
    private final Path processedDir;

    public FileActivitiesImpl(String workerId) {
        this.workerId = workerId;
        try {
            this.processedDir = Files.createTempDirectory("processed");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.deleteIfExists(this.processedDir);
                } catch (IOException e) {
                    // oh well
                }
            }));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public WorkerLocalFile createFile(String content) {
        logger.info("[%s] Creating file".formatted(workerId));
        try {
            Path tempFile = Files.createTempFile("processing", "tmp");
            Files.writeString(tempFile, content);
            String path = tempFile.toAbsolutePath().toString();
            logger.info("[%s] Created file %s".formatted(workerId, path));
            return new WorkerLocalFile(UUID.randomUUID().toString(), path, workerId);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void processFile(String path) {
        logger.info("[%s] Processing file %s".formatted(workerId, path));
        Path processingFile = Path.of(path);
        try {
            Files.move(processingFile, processedDir.resolve(processingFile.getFileName()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean uploadFile(String path) {
        logger.info("[%s] Uploading file %s".formatted(workerId, path));
        return ThreadLocalRandom.current().nextBoolean();
    }

    @Override
    public void deleteFile(String path) {
        logger.info("[%s] Deleting file %s".formatted(workerId, path));
        try {
            Files.deleteIfExists(Path.of(path));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void sendNotification(String path) {
        logger.info("[%s] File %s was successfully uploaded".formatted(workerId, path));
    }
}

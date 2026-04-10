package com.smartcache.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartcache.cache.RedisClient;
import com.smartcache.config.AppConfig;
import com.smartcache.model.Job;
import com.smartcache.service.ProcessorService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Worker pool — mirrors Go's worker/pool.go
 * Uses ExecutorService with fixed thread pool instead of goroutines.
 * Workers continuously poll the Redis queue via BLPOP.
 */
@Component
public class WorkerPool {

    private static final Logger log = LoggerFactory.getLogger(WorkerPool.class);

    private final AppConfig config;
    private final RedisClient cache;
    private final ProcessorService processor;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executorService;

    public WorkerPool(AppConfig config, RedisClient cache, ProcessorService processor) {
        this.config = config;
        this.cache = cache;
        this.processor = processor;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Starts the worker pool — mirrors Go's Pool.Start
     */
    @PostConstruct
    public void start() {
        int size = config.getWorkerCount();
        running.set(true);
        executorService = Executors.newFixedThreadPool(size);

        log.info("🚀 Starting {} workers...", size);
        for (int i = 1; i <= size; i++) {
            final int workerId = i;
            executorService.submit(() -> runWorker(workerId));
        }
    }

    /**
     * Graceful shutdown — mirrors Go's context.Cancel + signal handling
     */
    @PreDestroy
    public void stop() {
        log.info("🛑 Shutting down workers...");
        running.set(false);
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Workers did not terminate in time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("✅ Workers stopped");
    }

    /**
     * Main loop for a single worker — mirrors Go's Pool.runWorker
     */
    private void runWorker(int id) {
        log.info("Worker {} started", id);
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                String jobId = cache.popQueue();
                if (jobId == null) {
                    // Timeout on BLPOP, loop again to check running flag
                    continue;
                }
                processJob(id, jobId);
            } catch (Exception e) {
                if (Thread.currentThread().isInterrupted() || !running.get()) {
                    return;
                }
                log.error("Worker {}: queue error: {}", id, e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        log.info("Worker {} shutting down", id);
    }

    /**
     * Processes a single job — mirrors Go's Pool.processJob
     */
    private void processJob(int workerId, String jobId) {
        log.info("Worker {}: processing job {}", workerId, jobId);

        try {
            // Fetch job from Redis
            String raw = cache.getJob(jobId);
            if (raw == null) {
                log.error("Worker {}: failed to get job {}", workerId, jobId);
                return;
            }

            Job job = objectMapper.readValue(raw, Job.class);

            // Mark as processing
            job.setStatus(Job.STATUS_PROCESSING);
            cache.setJob(jobId, job);

            // Process (fetch URL if needed + AI call)
            ProcessorService.ProcessResult result = processor.process(jobId, job.getInput());

            // Update job with AI results
            job.setSummary(result.getSummary());
            job.setTags(result.getTags());
            job.setStatus(Job.STATUS_COMPLETED);
            job.setCompletedAt(Instant.now());
            job.setDurationMs(result.getDurationMs());

            cache.setJob(jobId, job);
            log.info("Worker {}: job {} completed in {}ms", workerId, jobId, result.getDurationMs());

        } catch (Exception e) {
            log.error("Worker {}: job {} failed: {}", workerId, jobId, e.getMessage());
            try {
                String raw = cache.getJob(jobId);
                if (raw != null) {
                    Job job = objectMapper.readValue(raw, Job.class);
                    job.setStatus(Job.STATUS_FAILED);
                    job.setError("Processing error: " + e.getMessage());
                    cache.setJob(jobId, job);
                }
            } catch (Exception ex) {
                log.error("Worker {}: failed to update failed job status: {}", workerId, ex.getMessage());
            }
        }
    }
}

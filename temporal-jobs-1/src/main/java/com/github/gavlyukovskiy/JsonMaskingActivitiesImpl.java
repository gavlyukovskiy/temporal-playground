package com.github.gavlyukovskiy;

import dev.blaauwendraad.masker.json.JsonMasker;
import dev.blaauwendraad.masker.json.config.JsonMaskingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.Duration;

public class JsonMaskingActivitiesImpl implements JsonMaskingActivities {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String workerId;
    private final JsonMasker jsonMasker;

    public JsonMaskingActivitiesImpl(String workerId) {
        this.workerId = workerId;
        this.jsonMasker = JsonMasker.getMasker(
                JsonMaskingConfig.builder()
                        .maskKeys("secret")
                        .maskStringsWith("[masked by temporal/%s]".formatted(workerId))
                        .maskNumbersWith("[masked by temporal/%s]".formatted(workerId))
                        .maskBooleansWith("[masked by temporal/%s]".formatted(workerId))
                        .build()
        );
    }

    @Override
    public String getCurrentWorkerQueue() {
        logger.info("[%s] Executing getCurrentWorkerQueue".formatted(workerId));
        return workerId;
    }

    @Override
    public String maskJson(String json) {
        logger.info("[%s] Executing maskJson".formatted(workerId));
        Utils.safeSleep(Duration.ofSeconds(5));
        return jsonMasker.mask(json);
    }
}

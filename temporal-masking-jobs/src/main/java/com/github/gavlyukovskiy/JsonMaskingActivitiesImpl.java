package com.github.gavlyukovskiy;

import dev.blaauwendraad.masker.json.InvalidJsonException;
import dev.blaauwendraad.masker.json.JsonMasker;
import dev.blaauwendraad.masker.json.config.JsonMaskingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.Set;

public class JsonMaskingActivitiesImpl implements JsonMaskingActivities {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String workerId;
    public JsonMaskingActivitiesImpl(String workerId) {
        this.workerId = workerId;
    }

    @Override
    public Set<String> getMaskingKeys(boolean strict) {
        return strict ? Set.of("secret", "password", "email") : Set.of("secret");
    }

    @Override
    public String maskJson(String json, Set<String> maskingKeys) {
        logger.info("[%s] Executing maskJson".formatted(workerId));
        var jsonMasker = JsonMasker.getMasker(
                        JsonMaskingConfig.builder()
                                .maskKeys("secret")
                                .maskStringsWith("[masked by temporal/%s]".formatted(workerId))
                                .maskNumbersWith("[masked by temporal/%s]".formatted(workerId))
                                .maskBooleansWith("[masked by temporal/%s]".formatted(workerId))
                                .build()
                );
        Utils.safeSleep(Duration.ofSeconds(5));
        try {
            return jsonMasker.mask(json);
        } catch (InvalidJsonException e) {
            throw new NonRetryableJsonException();
        }
    }
}

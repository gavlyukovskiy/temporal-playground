package com.github.gavlyukovskiy;

import java.time.Duration;

public class Utils {

    public static void safeSleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}

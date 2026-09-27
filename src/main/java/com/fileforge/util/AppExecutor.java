package com.fileforge.util;

import com.fileforge.database.SettingsEngine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutor {

    private static ExecutorService executorService = build();

    private AppExecutor() {}

    private static ExecutorService build() {
        int threads = SettingsEngine.getActiveSettings().getMaxProcessingThreads();
        return Executors.newFixedThreadPool(Math.max(1, threads));
    }

    public static ExecutorService get() {
        return executorService;
    }

    public static synchronized void reconfigure() {
        executorService.shutdown();
        executorService = build();
    }

    public static void shutdown() {
        executorService.shutdown();
    }
}
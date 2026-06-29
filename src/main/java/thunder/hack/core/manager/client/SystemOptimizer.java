package thunder.hack.core.manager.client;

import thunder.hack.ThunderHack;
import thunder.hack.core.manager.IManager;

import java.util.concurrent.atomic.AtomicLong;

import static thunder.hack.core.manager.IManager.mc;

public class SystemOptimizer implements IManager {
    private static long lastGcTime = 0;
    private static long lastNativeOptTime = 0;
    private static long lastTrimTime = 0;
    private static boolean fullOptimizeDone = false;
    private static final AtomicLong optCount = new AtomicLong(0);
    private static Thread worker;

    public static synchronized void start() {
        if (worker != null && worker.isAlive()) return;

        // Run full native optimization once on startup
        if (!fullOptimizeDone && NativeOptimizer.isLoaded()) {
            try {
                int count = NativeOptimizer.nativeFullOptimize();
                ThunderHack.LOGGER.info("[SystemOptimizer] Full native optimization applied ({} ops)", count);
                fullOptimizeDone = true;
            } catch (Throwable ignored) {}
        }

        worker = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                    if (mc == null || mc.world == null) continue;

                    long now = System.currentTimeMillis();

                    // Full native optimization every 60 seconds
                    if (now - lastNativeOptTime > 60_000 && NativeOptimizer.isLoaded()) {
                        runNativeOptimisation();
                        lastNativeOptTime = now;
                    }

                    // Working set trim every 15 seconds
                    if (now - lastTrimTime > 15_000 && NativeOptimizer.isLoaded()) {
                        NativeOptimizer.nativeTrimWorkingSet();
                        NativeOptimizer.nativeCompactHeap();
                        lastTrimTime = now;
                    }

                    // Java GC hint every 30 seconds
                    if (now - lastGcTime > 30_000) {
                        runJavaGcHint();
                        lastGcTime = now;
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ignored) {}
            }
        }, "ThunderHack-SystemOptimizer");

        worker.setDaemon(true);
        worker.setPriority(Thread.MIN_PRIORITY);
        worker.start();
    }

    public static void onWorldLoad() {
        if (NativeOptimizer.isLoaded()) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    NativeOptimizer.nativeFullOptimize();
                    NativeOptimizer.nativeSetThreadAffinity();
                    NativeOptimizer.nativeSetTimerResolution();
                    NativeOptimizer.nativePreventPowerThrottling();
                    ThunderHack.LOGGER.info("[SystemOptimizer] World-load optimizations applied");
                } catch (Throwable ignored) {}
            }, "ThunderHack-WorldOptimizer").start();
        }
        lastNativeOptTime = System.currentTimeMillis();
        lastTrimTime = System.currentTimeMillis();
    }

    public static void onGuiOpen() {
        if (NativeOptimizer.isLoaded()) {
            NativeOptimizer.nativeTrimWorkingSet();
            NativeOptimizer.nativeCompactHeap();
        }
        System.gc();
    }

    private static void runNativeOptimisation() {
        try {
            int freedKb = NativeOptimizer.nativeOptimize();
            optCount.incrementAndGet();
            if (freedKb > 0) {
                ThunderHack.LOGGER.info("[SystemOptimizer] Native optimisation freed ~{} KB (total runs: {})",
                        freedKb, optCount.get());
            }
        } catch (Throwable t) {
            ThunderHack.LOGGER.warn("[SystemOptimizer] Native optimisation failed: {}", t.getMessage());
        }
    }

    private static void runJavaGcHint() {
        System.gc();
        System.runFinalization();
    }

    public static long getTotalOptimisationCount() { return optCount.get(); }
}

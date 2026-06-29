package thunder.hack.core.manager.client;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeOptimizer {
    private static boolean loaded = false;
    private static String loadError = null;

    static {
        loadLibrary();
    }

    private static void loadLibrary() {
        if (loaded) return;

        String libName = System.mapLibraryName("thunderhack_optimizer");
        String resourcePath = "/assets/thunderhack/natives/" + libName;

        try {
            System.loadLibrary("thunderhack_optimizer");
            loaded = true;
            return;
        } catch (UnsatisfiedLinkError ignored) {}

        try (InputStream is = NativeOptimizer.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                loadError = "Native library not found in resources: " + resourcePath;
                return;
            }
            Path tmp = Files.createTempFile("thunderhack_optimizer", libName.contains(".") ? libName.substring(libName.lastIndexOf('.')) : "");
            tmp.toFile().deleteOnExit();
            Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            System.load(tmp.toAbsolutePath().toString());
            loaded = true;
        } catch (Throwable t) {
            loadError = t.getMessage();
        }
    }

    public static boolean isLoaded() { return loaded; }
    public static @Nullable String getLoadError() { return loadError; }

    // ── Core ──
    public static native int nativeTrimMemory();
    public static native int nativeSetHighPriority();
    public static native long nativeGetFreeMemory();
    public static native @Nullable String nativeGetSystemInfo();
    public static native int nativeOptimize();

    // ── Thread / CPU ──
    public static native int nativeSetThreadAffinity();
    public static native int nativeSetTimerResolution();
    public static native int nativePreventPowerThrottling();

    // ── Memory ──
    public static native int nativeSetMemoryPriority();
    public static native int nativeEnableLowFragmentationHeap();
    public static native int nativeTrimWorkingSet();
    public static native int nativeCompactHeap();

    // ── Network ──
    public static native int nativeDisableNagle();

    // ── System ──
    public static native int nativeClearSystemCaches();

    // ── Comprehensive ──
    public static native int nativeFullOptimize();
    public static native @Nullable String nativeGetDetailedSystemInfo();
}

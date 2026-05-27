package thunder.hack.utility.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

import java.util.HashMap;
import java.util.Map;

public class MSAAFramebuffer {
    public static final int MAX_SAMPLES = 0;
    private static final Map<Integer, MSAAFramebuffer> INSTANCES = new HashMap<>();

    private final int samples;

    public MSAAFramebuffer(int samples) {
        this.samples = samples;
    }

    public static MSAAFramebuffer getInstance(int samples) {
        return INSTANCES.computeIfAbsent(samples, x -> new MSAAFramebuffer(samples));
    }

    public static void use(boolean fancy, Runnable drawAction) {
        drawAction.run();
    }

    public static void use(int samples, Framebuffer mainBuffer, Runnable drawAction) {
        drawAction.run();
    }

    public void resize(int width, int height) {
    }

    public void initFbo(int width, int height) {
    }

    public void delete() {
    }
}

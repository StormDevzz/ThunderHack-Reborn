// RaveX Team — code interaction (https://github.com/StormDevzz/RaveX)
package thunder.hack.core.manager.client;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.modules.render.Shaders;
import thunder.hack.utility.interfaces.IShaderEffect;
import thunder.hack.utility.render.shaders.satin.api.managed.ManagedShaderEffect;
import thunder.hack.utility.render.shaders.satin.api.managed.ShaderEffectManager;

import java.util.ArrayList;
import java.util.List;

import static thunder.hack.features.modules.Module.mc;

public class ShaderManager implements IManager {
    private final static List<RenderTask> tasks = new ArrayList<>();
    private ThunderHackFramebuffer shaderBuffer;

    public float time = 0;

    public static ManagedShaderEffect DEFAULT_OUTLINE;
    public static ManagedShaderEffect SMOKE_OUTLINE;
    public static ManagedShaderEffect GRADIENT_OUTLINE;
    public static ManagedShaderEffect SNOW_OUTLINE;
    public static ManagedShaderEffect FADE_OUTLINE;

    public static ManagedShaderEffect DEFAULT;
    public static ManagedShaderEffect SMOKE;
    public static ManagedShaderEffect GRADIENT;
    public static ManagedShaderEffect SNOW;
    public static ManagedShaderEffect FADE;

    public void renderShader(Runnable runnable, Shader mode) {
        tasks.add(new RenderTask(runnable, mode));
    }

    public void renderShaders() {
        if (DEFAULT == null) {
            shaderBuffer = new ThunderHackFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
            reloadShaders();
        }

        if (shaderBuffer == null)
            return;

        tasks.forEach(t -> applyShader(t.task(), t.shader()));
        tasks.clear();
    }

    public void applyShader(Runnable runnable, Shader mode) {
        // 1.21.11 rendering API stubbed - functionality disabled
    }

    public ManagedShaderEffect getShader(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT;
            case Smoke -> SMOKE;
            case Snow -> SNOW;
            case Fade -> FADE;
            default -> DEFAULT;
        };
    }

    public ManagedShaderEffect getShaderOutline(@NotNull Shader mode) {
        return switch (mode) {
            case Gradient -> GRADIENT_OUTLINE;
            case Smoke -> SMOKE_OUTLINE;
            case Snow -> SNOW_OUTLINE;
            case Fade -> FADE_OUTLINE;
            default -> DEFAULT_OUTLINE;
        };
    }

    public void reloadShaders() {
        DEFAULT = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/default.json"));
        SMOKE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/smoke.json"));
        GRADIENT = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/gradient.json"));
        SNOW = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/snow.json"));
        FADE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/fade.json"));

        DEFAULT_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/default.json"));
        SMOKE_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/smoke.json"));
        GRADIENT_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/gradient.json"));
        SNOW_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/snow.json"));
        FADE_OUTLINE = ShaderEffectManager.getInstance().manage(Identifier.of("thunderhack", "shaders/post/fade.json"));
    }

    public static class ThunderHackFramebuffer extends SimpleFramebuffer {
        public ThunderHackFramebuffer(int width, int height) {
            super("thunderhack", width, height, false);
        }
    }

    public boolean fullNullCheck() {
        if (GRADIENT == null || SMOKE == null || DEFAULT == null) {
            shaderBuffer = new ThunderHackFramebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight);
            reloadShaders();
            return true;
        }

        return false;
    }

    public record RenderTask(Runnable task, Shader shader) {
    }

    public enum Shader {
        Default,
        Smoke,
        Gradient,
        Snow,
        Fade
    }
}

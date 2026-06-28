package thunder.hack.utility.render.shaders.satin.impl;

import net.minecraft.util.Identifier;
import thunder.hack.utility.render.shaders.satin.api.managed.*;

public final class ReloadableShaderEffectManager implements ShaderEffectManager {
    public static final ReloadableShaderEffectManager INSTANCE = new ReloadableShaderEffectManager();

    public ReloadableShaderEffectManager() {
    }

    public void reload(net.minecraft.resource.ResourceFactory shaderResources) {
    }

    @Override
    public ManagedShaderEffect manage(Identifier location) {
        return null;
    }

    @Override
    public ManagedShaderEffect manage(Identifier location, java.util.function.Consumer<ManagedShaderEffect> initCallback) {
        return null;
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location) {
        return null;
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location, com.mojang.blaze3d.vertex.VertexFormat vertexFormat) {
        return null;
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location, com.mojang.blaze3d.vertex.VertexFormat vertexFormat, java.util.function.Consumer<ManagedCoreShader> initCallback) {
        return null;
    }
}

package thunder.hack.injection;

import org.spongepowered.asm.mixin.Mixin;
import thunder.hack.utility.render.shaders.satin.impl.SamplerAccess;
import net.minecraft.client.gl.ShaderProgram;

@Mixin(ShaderProgram.class)
public abstract class MixinCoreShader implements SamplerAccess {
}

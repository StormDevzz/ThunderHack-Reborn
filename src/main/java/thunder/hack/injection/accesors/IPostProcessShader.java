package thunder.hack.injection.accesors;

import net.minecraft.client.gl.PostEffectPass;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PostEffectPass.class)
public interface IPostProcessShader {
}

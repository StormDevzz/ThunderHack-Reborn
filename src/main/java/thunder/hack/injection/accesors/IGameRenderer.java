package thunder.hack.injection.accesors;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRenderer.class)
public interface IGameRenderer {
}

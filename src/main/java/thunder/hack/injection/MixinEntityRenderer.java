package thunder.hack.injection;

import net.minecraft.entity.decoration.ArmorStandEntity;
import thunder.hack.core.manager.client.ModuleManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void renderLabelIfPresent(EntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue commandQueue, CameraRenderState cameraState, CallbackInfo ci) {
        if (th$lastEntity instanceof ArmorStandEntity && ModuleManager.noRender.isEnabled() && ModuleManager.noRender.noArmorStands.getValue())
            ci.cancel();

        if (entity instanceof PlayerEntity && ModuleManager.nameTags.isEnabled())
            info.cancel();
    }
}
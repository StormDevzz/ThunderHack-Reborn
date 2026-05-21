package thunder.hack.injection;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.models.ItemModel;
import net.minecraft.client.render.item.render.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.render.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.render.GlintColor;

import java.awt.Color;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Unique
    private void applyGlintColor(Color color) {
        if (color != null) {
            RenderSystem.setShaderColor(
                color.getRed() / 255.0f,
                color.getGreen() / 255.0f,
                color.getBlue() / 255.0f,
                color.getAlpha() / 255.0f
            );
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @Inject(method = "renderGuiItemOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderSystem;enableBlend()V", ordinal = 0))
    public void onRenderGlintStart(MatrixStack matrices, VertexConsumer vertices, ItemRenderState state, int light, CallbackInfo ci) {
        GlintColor module = ModuleManager.glintColor;
        if (module != null && module.shouldOverrideGlint()) {
            Color glintColor = module.getGlintColor();
            applyGlintColor(glintColor);
        }
    }

    @Inject(method = "renderGuiItemOverlay", at = @At("RETURN"))
    public void onRenderGlintEnd(MatrixStack matrices, VertexConsumer vertices, ItemRenderState state, int light, CallbackInfo ci) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}

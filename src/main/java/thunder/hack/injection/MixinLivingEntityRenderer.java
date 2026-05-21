package thunder.hack.injection;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.injection.accesors.IClientPlayerEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.util.List;

import static thunder.hack.features.modules.Module.mc;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends net.minecraft.client.render.entity.state.LivingEntityRenderState, M extends EntityModel<? super S>> {
    private LivingEntity lastEntity;

    private float originalHeadYaw, originalPrevHeadYaw, originalPrevHeadPitch, originalHeadPitch;

    @Shadow
    protected M model;

    @Shadow
    @Final
    protected List<FeatureRenderer> features;

    @Unique
    private boolean matrixPushed;

    @Inject(method = "updateRenderState", at = @At("HEAD"))
    private void onUpdateRenderState(T entity, S renderState, float tickDelta, CallbackInfo ci) {
        if (Module.fullNullCheck()) return;
        lastEntity = entity;
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    public void onRenderPre(net.minecraft.client.render.entity.state.LivingEntityRenderState state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (Module.fullNullCheck()) return;

        matrixPushed = false;

        if (ModuleManager.chams.isEnabled() && ModuleManager.chams.players.getValue()
                && lastEntity instanceof PlayerEntity pl && lastEntity != mc.player) {
            ci.cancel();
            ModuleManager.chams.renderPlayer(state, matrixStack, i, model, pl);
            return;
        }

        if (lastEntity instanceof PlayerEntity pl) {
            if (ModuleManager.smallUser.isEnabled() && ModuleManager.smallUser.shouldMakeSmall(pl)) {
                matrixStack.push();
                matrixPushed = true;
                matrixStack.scale(0.5f, 0.5f, 0.5f);
            }
        }
    }

    @Unique
    public void postRender(net.minecraft.client.render.entity.state.LivingEntityRenderState state) {
        if (Module.fullNullCheck()) return;
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    public void onRenderPost(net.minecraft.client.render.entity.state.LivingEntityRenderState state, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (Module.fullNullCheck()) return;

        if (ModuleManager.chinaHat.isEnabled() && lastEntity instanceof PlayerEntity pl && ModuleManager.chinaHat.shouldRender(pl)) {
            matrixStack.push();
            matrixStack.translate(0.0f, -0.249f, 0.0f);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();

            java.awt.Color color = ModuleManager.chinaHat.color.getValue().getColorObject();
            RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
            RenderSystem.setShader(ShaderProgramKeys.POSITION);

            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);

            float radius = 0.3f;
            float height = 0.4f;
            int segments = 16;

            for (int seg = 0; seg < segments; seg++) {
                float a1 = (float) (seg * 2 * Math.PI / segments);
                float a2 = (float) ((seg + 1) * 2 * Math.PI / segments);
                float x1 = radius * MathHelper.cos(a1);
                float z1 = radius * MathHelper.sin(a1);
                float x2 = radius * MathHelper.cos(a2);
                float z2 = radius * MathHelper.sin(a2);

                buffer.vertex(matrixStack.peek().getPositionMatrix(), x1, 0, z1);
                buffer.vertex(matrixStack.peek().getPositionMatrix(), 0, height, 0);
                buffer.vertex(matrixStack.peek().getPositionMatrix(), x2, 0, z2);
            }

            Render2DEngine.endBuilding(buffer);

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();

            matrixStack.pop();
        }

        if (matrixPushed) {
            matrixStack.pop();
            matrixPushed = false;
        }
        postRender(state);
    }

    /*
    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void renderHook(Args args) {
        if (Module.fullNullCheck()) return;

        float alpha = -1f;

        if (ModuleManager.noRender.isEnabled() && ModuleManager.noRender.antiPlayerCollision.getValue() && lastEntity != mc.player && lastEntity instanceof PlayerEntity pl && !pl.isInvisible())
            alpha = MathUtility.clamp((float) (mc.player.squaredDistanceTo(lastEntity.getPos()) / 3f) + 0.2f, 0f, 1f);

        if (lastEntity != mc.player && lastEntity instanceof PlayerEntity pl && pl.isInvisible() && ModuleManager.serverHelper.isEnabled() && ModuleManager.serverHelper.trueSight.getValue())
            alpha = 0.3f;

        if (alpha != -1)
            args.set(4, Render2DEngine.applyOpacity(0x26FFFFFF, alpha));
    }
    */
}
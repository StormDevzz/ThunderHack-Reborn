package thunder.hack.injection;

// RaveX Team — code interaction (https://github.com/StormDevzz/RaveX)

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.state.CameraRenderState;
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

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    public void onRenderPre(net.minecraft.client.render.entity.state.LivingEntityRenderState state, MatrixStack matrixStack, OrderedRenderCommandQueue commandQueue, CameraRenderState cameraState, CallbackInfo ci) {
        if (Module.fullNullCheck()) return;

        matrixPushed = false;

        if (ModuleManager.chams.isEnabled() && ModuleManager.chams.players.getValue()
                && lastEntity instanceof PlayerEntity pl && lastEntity != mc.player) {
            ci.cancel();
            ModuleManager.chams.renderPlayer(state, matrixStack, 0, model, pl);
            return;
        }

        if (lastEntity instanceof PlayerEntity pl) {
        }
    }

    @Unique
    public void postRender(net.minecraft.client.render.entity.state.LivingEntityRenderState state) {
        if (Module.fullNullCheck()) return;
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At("TAIL"))
    public void onRenderPost(net.minecraft.client.render.entity.state.LivingEntityRenderState state, MatrixStack matrixStack, OrderedRenderCommandQueue commandQueue, CameraRenderState cameraState, CallbackInfo ci) {
        if (Module.fullNullCheck()) return;

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
            alpha = MathUtility.clamp((float) (mc.player.squaredDistanceTo(lastEntity.getEntityPos()) / 3f) + 0.2f, 0f, 1f);

        if (lastEntity != mc.player && lastEntity instanceof PlayerEntity pl && pl.isInvisible() && ModuleManager.serverHelper.isEnabled() && ModuleManager.serverHelper.trueSight.getValue())
            alpha = 0.3f;

        if (alpha != -1)
            args.set(4, Render2DEngine.applyOpacity(0x26FFFFFF, alpha));
    }
    */
}
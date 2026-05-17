package thunder.hack.features.modules.render;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import thunder.hack.events.impl.*;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

public class FreeCam extends Module {
    public FreeCam() {
        super("FreeCam", Category.RENDER);
    }

    private final Setting<Float> speed = new Setting<>("HSpeed", 1f, 0.1f, 3f);
    private final Setting<Float> hspeed = new Setting<>("VSpeed", 0.42f, 0.1f, 3f);
    private final Setting<Boolean> freeze = new Setting<>("Freeze", false);
    public final Setting<Boolean> track = new Setting<>("Track", false);
    private final Setting<Float> cameraSpeed = new Setting<>("CameraSpeed", 0.2f, 0.1f, 5f);
    private final Setting<Boolean> invertPitch = new Setting<>("InvertPitch", false);

    private float fakeYaw, fakePitch, prevFakeYaw, prevFakePitch, prevScroll;
    private float lockedYaw, lockedPitch;
    private double fakeX, fakeY, fakeZ, prevFakeX, prevFakeY, prevFakeZ;
    public LivingEntity trackEntity;

    @Override
    public void onEnable() {
        mc.chunkCullingEnabled = false;
        trackEntity = null;

        fakePitch = mc.player.getPitch();
        fakeYaw = mc.player.getYaw();
        lockedYaw = fakeYaw;
        lockedPitch = fakePitch;

        prevFakePitch = fakePitch;
        prevFakeYaw = fakeYaw;

        fakeX = mc.player.getX();
        fakeY = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
        fakeZ = mc.player.getZ();

        prevFakeX = mc.player.getX();
        prevFakeY = mc.player.getY();
        prevFakeZ = mc.player.getZ();
    }

    @EventHandler
    public void onAttack(EventAttack e) {
        if (!e.isPre() && e.getEntity() instanceof LivingEntity entity && track.getValue())
            trackEntity = entity;
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) return;
        mc.chunkCullingEnabled = true;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSync(EventSync e) {
        mc.player.setYaw(lockedYaw);
        mc.player.setPitch(lockedPitch);
        mc.player.prevYaw = lockedYaw;
        mc.player.prevPitch = lockedPitch;

        prevFakeYaw = fakeYaw;
        prevFakePitch = fakePitch;

        if (isKeyPressed(GLFW.GLFW_KEY_ESCAPE) || isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT))
            trackEntity = null;

        if (trackEntity != null) {
            fakeYaw = trackEntity.getYaw();
            fakePitch = trackEntity.getPitch();

            prevFakeX = fakeX;
            prevFakeY = fakeY;
            prevFakeZ = fakeZ;

            fakeX = trackEntity.getX();
            fakeY = trackEntity.getY() + trackEntity.getEyeHeight(trackEntity.getPose());
            fakeZ = trackEntity.getZ();
        }
    }


    public void handleMouseYaw(double deltaYaw) {
        fakeYaw += (float) (deltaYaw * cameraSpeed.getValue());
    }

    public void handleMousePitch(double deltaPitch) {
        float pitch = (invertPitch.getValue() ? -1f : 1f) * (float) (deltaPitch * cameraSpeed.getValue());
        fakePitch = MathHelper.clamp(fakePitch + pitch, -90f, 90f);
    }

    @EventHandler
    public void onKeyboardInput(EventKeyboardInput e) {
        if (mc.player == null) return;

        if (trackEntity == null) {
            float forward = mc.player.input.movementForward;
            float strafe = mc.player.input.movementSideways;
            float yaw = fakeYaw;

            if (forward != 0.0f) {
                if (strafe > 0.0f) {
                    yaw += (forward > 0.0f) ? -45 : 45;
                } else if (strafe < 0.0f) {
                    yaw += (forward > 0.0f) ? 45 : -45;
                }
                strafe = 0.0f;
                if (forward > 0.0f) forward = 1.0f;
                else if (forward < 0.0f) forward = -1.0f;
            }

            double sin = Math.sin(Math.toRadians(yaw + 90.0f));
            double cos = Math.cos(Math.toRadians(yaw + 90.0f));

            prevFakeX = fakeX;
            prevFakeY = fakeY;
            prevFakeZ = fakeZ;

            fakeX += forward * speed.getValue() * cos + strafe * speed.getValue() * sin;
            fakeZ += forward * speed.getValue() * sin - strafe * speed.getValue() * cos;

            if (mc.options.jumpKey.isPressed())
                fakeY += hspeed.getValue();

            if (mc.options.sneakKey.isPressed())
                fakeY -= hspeed.getValue();
        }

        mc.player.input.movementForward = 0;
        mc.player.input.movementSideways = 0;
        mc.player.input.jumping = false;
        mc.player.input.sneaking = false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMove(EventMove e) {
        if (freeze.getValue()) {
            e.setX(0.);
            e.setY(0.);
            e.setZ(0.);
            e.cancel();
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (freeze.getValue() && e.getPacket() instanceof PlayerMoveC2SPacket)
            e.cancel();
    }

    @EventHandler
    public void onScroll(EventMouse e) {
        if (e.getAction() == 2) {
            if (e.getButton() > 0) speed.setValue(speed.getValue() + 0.05f);
            else speed.setValue(speed.getValue() - 0.05f);
            prevScroll = e.getButton();
        }
    }

    public float getFakeYaw() {
        return (float) Render2DEngine.interpolate(prevFakeYaw, fakeYaw, Render3DEngine.getTickDelta());
    }

    public float getFakePitch() {
        return (float) Render2DEngine.interpolate(prevFakePitch, fakePitch, Render3DEngine.getTickDelta());
    }

    public double getFakeX() {
        return Render2DEngine.interpolate(prevFakeX, fakeX, Render3DEngine.getTickDelta());
    }

    public double getFakeY() {
        return Render2DEngine.interpolate(prevFakeY, fakeY, Render3DEngine.getTickDelta());
    }

    public double getFakeZ() {
        return Render2DEngine.interpolate(prevFakeZ, fakeZ, Render3DEngine.getTickDelta());
    }
}

package thunder.hack.features.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class FreeLook extends Module {
    public FreeLook() {
        super("FreeLook", Category.RENDER);
    }

    private final Setting<Float> distance = new Setting<>("Distance", 4.0f, 1.0f, 10.0f);
    private final Setting<Float> speed = new Setting<>("Speed", 1.0f, 0.1f, 2.0f);

    private float lockedYaw, lockedPitch;
    private float cameraYaw, cameraPitch;
    private boolean switchedToThirdPerson;

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        lockedYaw = mc.player.getYaw();
        lockedPitch = mc.player.getPitch();

        cameraYaw = lockedYaw;
        cameraPitch = lockedPitch;

        switchedToThirdPerson = mc.options.getPerspective().isFirstPerson();
        if (switchedToThirdPerson) {
            mc.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        if (switchedToThirdPerson) {
            mc.options.setPerspective(net.minecraft.client.option.Perspective.FIRST_PERSON);
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (mc.player == null) return;

        // Player rotation is now handled by MixinEntity and handleMouse methods
        mc.player.setYaw(lockedYaw);
        mc.player.setPitch(lockedPitch);
        mc.player.prevYaw = lockedYaw;
        mc.player.prevPitch = lockedPitch;
    }

    public void handleMouseYaw(double deltaYaw) {
        cameraYaw += (float) (deltaYaw * speed.getValue());
    }

    public void handleMousePitch(double deltaPitch) {
        cameraPitch = MathHelper.clamp(cameraPitch + (float) (deltaPitch * speed.getValue()), -90.0f, 90.0f);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (mc.player == null) return;
        if (e.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround) {
                e.cancel();
                mc.player.networkHandler.getConnection().send(
                    new PlayerMoveC2SPacket.LookAndOnGround(lockedYaw, lockedPitch, mc.player.isOnGround()),
                    null
                );
            } else if (packet instanceof PlayerMoveC2SPacket.Full full) {
                e.cancel();
                mc.player.networkHandler.getConnection().send(
                    new PlayerMoveC2SPacket.Full(full.getX(mc.player.getX()), full.getY(mc.player.getY()), full.getZ(mc.player.getZ()), lockedYaw, lockedPitch, mc.player.isOnGround()),
                    null
                );
            }
        }
    }

    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }
}
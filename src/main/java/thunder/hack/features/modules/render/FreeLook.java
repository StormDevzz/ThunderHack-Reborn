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
    private double lockedX, lockedY, lockedZ;

    private float cameraYaw, cameraPitch;
    private float lastPlayerYaw, lastPlayerPitch;

    private double camX, camY, camZ;

    private boolean switchedToThirdPerson;

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        lockedYaw = mc.player.getYaw();
        lockedPitch = mc.player.getPitch();
        lockedX = mc.player.getX();
        lockedY = mc.player.getY();
        lockedZ = mc.player.getZ();

        cameraYaw = lockedYaw;
        cameraPitch = lockedPitch;
        lastPlayerYaw = lockedYaw;
        lastPlayerPitch = lockedPitch;

        // Если игрок в первом лице — переключаем на третье лицо (F5)
        switchedToThirdPerson = mc.options.getPerspective().isFirstPerson();
        if (switchedToThirdPerson) {
            mc.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
        }

        updateCameraPosition();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        // Возвращаем первое лицо, только если модуль сам переключил на третье
        if (switchedToThirdPerson) {
            mc.options.setPerspective(net.minecraft.client.option.Perspective.FIRST_PERSON);
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (mc.player == null) return;

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        float dYaw = currentYaw - lastPlayerYaw;
        float dPitch = currentPitch - lastPlayerPitch;

        if (dYaw > 180) dYaw -= 360;
        if (dYaw < -180) dYaw += 360;

        cameraYaw += dYaw * speed.getValue();
        cameraPitch = MathHelper.clamp(cameraPitch + dPitch * speed.getValue(), -90.0f, 90.0f);

        lastPlayerYaw = currentYaw;
        lastPlayerPitch = currentPitch;

        updateCameraPosition();

        mc.player.setYaw(lockedYaw);
        mc.player.setPitch(lockedPitch);
        mc.player.setPosition(lockedX, lockedY, lockedZ);
    }

    private void updateCameraPosition() {
        double radYaw = Math.toRadians(cameraYaw);
        double radPitch = Math.toRadians(cameraPitch);
        double cosYaw = Math.cos(radYaw);
        double sinYaw = Math.sin(radYaw);
        double cosPitch = Math.cos(radPitch);

        camX = lockedX - sinYaw * cosPitch * distance.getValue();
        camY = lockedY + mc.player.getEyeHeight(mc.player.getPose()) - Math.sin(radPitch) * distance.getValue();
        camZ = lockedZ + cosYaw * cosPitch * distance.getValue();
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
                    new PlayerMoveC2SPacket.Full(lockedX, lockedY, lockedZ, lockedYaw, lockedPitch, mc.player.isOnGround()),
                    null
                );
            } else if (packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
                e.cancel();
                mc.player.networkHandler.getConnection().send(
                    new PlayerMoveC2SPacket.PositionAndOnGround(lockedX, lockedY, lockedZ, mc.player.isOnGround()),
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

    public double getCameraX() {
        return camX;
    }

    public double getCameraY() {
        return camY;
    }

    public double getCameraZ() {
        return camZ;
    }
}
package thunder.hack.features.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;

public class FreeLook extends Module {
    public FreeLook() {
        super("FreeLook", Category.RENDER);
    }

    // Переключатель режима камеры: true = вид от 3-го лица (по умолчанию), false = от 1-го
    private boolean thirdPerson = true;
    
    // Замороженные координаты и углы игрока, которые будут отправляться на сервер
    private float lockedYaw, lockedPitch;
    private double lockedX, lockedY, lockedZ;
    
    // Текущие значения поворота камеры (freelook)
    private float cameraYaw, cameraPitch;
    private float prevCameraYaw, prevCameraPitch;

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        // Запоминаем позицию и поворот игрока на момент включения
        lockedYaw = mc.player.getYaw();
        lockedPitch = mc.player.getPitch();
        lockedX = mc.player.getX();
        lockedY = mc.player.getY();
        lockedZ = mc.player.getZ();

        // Начальное положение камеры совпадает с locked
        cameraYaw = lockedYaw;
        cameraPitch = lockedPitch;
        prevCameraYaw = lockedYaw;
        prevCameraPitch = lockedPitch;

        // Принудительно включаем режим от 3-го лица для наглядности
        if (thirdPerson) {
            mc.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (mc.player == null) return;

        // 1. Вычисляем, насколько игрок попытался повернуться за этот тик
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float dYaw = currentYaw - lockedYaw;
        float dPitch = currentPitch - lockedPitch;

        // Нормализуем dYaw в диапазон [-180, 180)
        if (dYaw > 180) dYaw -= 360;
        if (dYaw < -180) dYaw += 360;

        // 2. Применяем эту разницу к камере
        cameraYaw += dYaw;
        cameraPitch = MathHelper.clamp(cameraPitch + dPitch, -90f, 90f);

        // 3. Возвращаем игрока в исходное положение (блокируем его поворот)
        mc.player.setYaw(lockedYaw);
        mc.player.setPitch(lockedPitch);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (mc.player == null) return;
        if (e.getPacket() instanceof PlayerMoveC2SPacket packet) {
            // Блокируем все пакеты, которые могут выдать поворот камеры на сервер
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
}
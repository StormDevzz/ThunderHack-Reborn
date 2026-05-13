package thunder.hack.features.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import thunder.hack.events.impl.EventSync;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.features.modules.Module;

public class FreeLook extends Module {
    public FreeLook() {
        super("FreeLook", Category.RENDER);
    }

    // Фиксированные позиция и поворот персонажа для сервера
    private float lockedYaw, lockedPitch;
    private double lockedX, lockedY, lockedZ;

    // Текущие накопленные углы камеры
    private float cameraYaw, cameraPitch;

    // Предыдущие углы игрока, чтобы вычислить дельту от мыши
    private float lastPlayerYaw, lastPlayerPitch;

    // Сохранённая перспектива до включения модуля
    private net.minecraft.client.option.Perspective previousPerspective;
    // Была ли перспектива третьим лицом изначально
    private boolean wasThirdPerson;

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        // Запоминаем положение и поворот игрока на момент включения
        lockedYaw = mc.player.getYaw();
        lockedPitch = mc.player.getPitch();
        lockedX = mc.player.getX();
        lockedY = mc.player.getY();
        lockedZ = mc.player.getZ();

        // Камера стартует с того же направления
        cameraYaw = lockedYaw;
        cameraPitch = lockedPitch;

        // Для отслеживания дельты мыши
        lastPlayerYaw = lockedYaw;
        lastPlayerPitch = lockedPitch;

        // Сохраняем текущую перспективу
        previousPerspective = mc.options.getPerspective();
        wasThirdPerson = previousPerspective.isFirstPerson() == false;

        // Переключаемся на третье лицо, только если сейчас первое
        if (!wasThirdPerson) {
            mc.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        // Возвращаем перспективу, только если она была изменена модулем
        if (!wasThirdPerson) {
            mc.options.setPerspective(previousPerspective);
        }
    }

    @EventHandler
    public void onSync(EventSync e) {
        if (mc.player == null) return;

        // 1. Получаем текущий поворот игрока (уже с учётом мыши)
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        // 2. Вычисляем дельту, которую сделала мышь за этот тик
        float dYaw = currentYaw - lastPlayerYaw;
        float dPitch = currentPitch - lastPlayerPitch;

        // Нормализация рыскания
        if (dYaw > 180) dYaw -= 360;
        if (dYaw < -180) dYaw += 360;

        // 3. Применяем дельту к камере
        cameraYaw += dYaw;
        cameraPitch = MathHelper.clamp(cameraPitch + dPitch, -90f, 90f);

        // 4. Обновляем «последние» углы для следующего тика
        lastPlayerYaw = currentYaw;
        lastPlayerPitch = currentPitch;

        // 5. Возвращаем персонажа в замороженное состояние (сервер не видит поворотов)
        mc.player.setYaw(lockedYaw);
        mc.player.setPitch(lockedPitch);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (mc.player == null) return;
        if (e.getPacket() instanceof PlayerMoveC2SPacket packet) {
            // Подменяем пакеты с поворотом на замороженные значения
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

    // Геттеры для миксина камеры
    public float getCameraYaw() {
        return cameraYaw;
    }

    public float getCameraPitch() {
        return cameraPitch;
    }
}
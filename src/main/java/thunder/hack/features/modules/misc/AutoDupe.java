package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.events.impl.EventSync;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.MovementUtility;

import java.util.Comparator;
import java.util.List;

public class AutoDupe extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Donkey);

    // Donkey/Kill Dupe Settings
    private final Setting<Integer> donkeyDelay = new Setting<>("DonkeyDelay", 1000, 100, 5000, v -> mode.getValue() == Mode.Donkey);
    private final Setting<Boolean> donkeySave = new Setting<>("DonkeySave", false, v -> mode.getValue() == Mode.Donkey);
    private final Setting<Boolean> autoDisable = new Setting<>("AutoDisable", true, v -> mode.getValue() == Mode.Donkey);

    // Frame Dupe Settings
    private final Setting<Integer> frameDelay = new Setting<>("FrameDelay", 200, 0, 1000, v -> mode.getValue() == Mode.Frame);
    private final Setting<Boolean> frameRotate = new Setting<>("FrameRotate", true, v -> mode.getValue() == Mode.Frame);
    private final Setting<Boolean> frameSilent = new Setting<>("FrameSilent", true, v -> mode.getValue() == Mode.Frame);

    // Mine-Place Dupe Settings
    private final Setting<Integer> minePlaceTimes = new Setting<>("MineTimes", 2, 1, 5, v -> mode.getValue() == Mode.MinePlace);
    private final Setting<Integer> minePlaceDelay = new Setting<>("MineDelay", 50, 0, 500, v -> mode.getValue() == Mode.MinePlace);

    // Chicken Dupe Settings
    private final Setting<Integer> chickenDelay = new Setting<>("ChickenDelay", 1000, 100, 5000, v -> mode.getValue() == Mode.Chicken);
    private final Setting<Boolean> chickenRightClick = new Setting<>("ChickenRClick", true, v -> mode.getValue() == Mode.Chicken);

    // Inventory Dupe Settings
    private final Setting<Integer> inventoryDelay = new Setting<>("InvDelay", 100, 0, 500, v -> mode.getValue() == Mode.Inventory);
    private final Setting<Boolean> invDoubleClick = new Setting<>("InvDoubleClick", true, v -> mode.getValue() == Mode.Inventory);

    // Book & Quill Dupe Settings
    private final Setting<Integer> bookDelay = new Setting<>("BookDelay", 500, 100, 5000, v -> mode.getValue() == Mode.Book);

    public enum Mode { Donkey, Frame, MinePlace, Chicken, Inventory, Book }

    private final Timer timer = new Timer();
    private BlockPos targetPos;
    private int stage;

    public AutoDupe() {
        super("AutoDupe", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            disable();
            return;
        }
        stage = 0;
        targetPos = mc.player.getBlockPos();
        timer.reset();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.passedMs(mode.getValue() == Mode.Frame ? frameDelay.getValue() : 
            mode.getValue() == Mode.MinePlace ? minePlaceDelay.getValue() :
            mode.getValue() == Mode.Chicken ? chickenDelay.getValue() :
            mode.getValue() == Mode.Inventory ? inventoryDelay.getValue() :
            mode.getValue() == Mode.Book ? bookDelay.getValue() : donkeyDelay.getValue())) return;

        switch (mode.getValue()) {
            case Donkey -> handleDonkeyDupe();
            case Frame -> handleFrameDupe();
            case MinePlace -> handleMinePlaceDupe();
            case Chicken -> handleChickenDupe();
            case Inventory -> handleInventoryDupe();
            case Book -> handleBookDupe();
        }
    }

    // === РЕЖИМЫ ДЮПА ===

    private void handleDonkeyDupe() {
        Entity vehicle = mc.player.getVehicle();
        if (vehicle instanceof DonkeyEntity || vehicle instanceof MuleEntity || vehicle instanceof LlamaEntity || vehicle instanceof ChestBoatEntity) {
            // Классический метод: отправка пакета взаимодействия
            mc.player.networkHandler.sendPacket(
                PlayerInteractEntityC2SPacket.interact(vehicle, mc.player.isSneaking(), Hand.MAIN_HAND)
            );

            // Сохранение данных — команда для синхронизации
            if (donkeySave.getValue()) {
                mc.player.networkHandler.sendCommand("msg");
            }

            stage++;
            timer.reset();

            if (autoDisable.getValue() && stage >= 2) {
                sendMessage("§a[AutoDupe] Donkey dupe completed! Check inventory.");
                toggle();
            }
        } else {
            sendMessage("§c[AutoDupe] Not riding a donkey/mule/llama/chest boat! Disabling...");
            toggle();
        }
    }

    private void handleFrameDupe() {
        // Ищем ближайшие рамки
        List<ItemFrameEntity> frames = mc.world.getEntitiesByClass(ItemFrameEntity.class,
            new Box(mc.player.getPos().add(-5, -5, -5), mc.player.getPos().add(5, 5, 5)),
            frame -> !frame.getHeldItemStack().isEmpty()
        );

        ItemFrameEntity frame = frames.stream()
            .min(Comparator.comparingDouble(f -> f.squaredDistanceTo(mc.player)))
            .orElse(null);

        if (frame != null) {
            // Поворот предмета в рамке для активации дюпа
            if (frameRotate.getValue()) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }

            mc.interactionManager.attackEntity(mc.player, frame);
            mc.player.swingHand(Hand.MAIN_HAND);

            if (frameSilent.getValue()) {
                mc.player.setPitch(90); // Взгляд вниз для быстрого подбора
            }

            timer.reset();
        }
    }

    private void handleMinePlaceDupe() {
        if (mc.crosshairTarget instanceof BlockHitResult hitResult) {
            targetPos = hitResult.getBlockPos();
        } else return;

        // Имитация мгновенного майнинга с многократным нажатием
        for (int i = 0; i < minePlaceTimes.getValue(); i++) {
            mc.interactionManager.attackBlock(targetPos, Direction.UP);
        }
        mc.player.swingHand(Hand.MAIN_HAND);

        timer.reset();
        stage++;
    }

    private void handleChickenDupe() {
        List<ChickenEntity> chickens = mc.world.getEntitiesByClass(ChickenEntity.class,
            new Box(mc.player.getPos().add(-5, -5, -5), mc.player.getPos().add(5, 5, 5)),
            chicken -> true
        );

        if (!chickens.isEmpty()) {
            ChickenEntity chicken = chickens.get(0);
            if (chickenRightClick.getValue()) {
                // Правый клик для дюпа через курицу
                mc.interactionManager.interactEntity(mc.player, chicken, Hand.MAIN_HAND);
            } else {
                // Левый клик для убийства и получения двойного лута
                mc.interactionManager.attackEntity(mc.player, chicken);
            }
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
        } else {
            sendMessage("§c[AutoDupe] No chickens nearby!");
        }
    }

    private void handleInventoryDupe() {
        // Проверка на открытый контейнер
        if (!(mc.player.currentScreenHandler != mc.player.playerScreenHandler)) {
            sendMessage("§c[AutoDupe] Open a container first!");
            return;
        }

        int slot = mc.player.getInventory().selectedSlot;

        if (invDoubleClick.getValue()) {
            // Двойной клик для дюпа через быстрый обмен
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
        } else {
            // Shift+клик для массового перемещения
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.QUICK_MOVE, mc.player);
        }

        timer.reset();
    }

    private void handleBookDupe() {
        // Book & Quill Dupe для Paper 1.21 (не работает на Purpur)
        int bookSlot = InventoryUtility.findItemInHotBar(Items.WRITABLE_BOOK).slot();
        if (bookSlot == -1) {
            sendMessage("§c[AutoDupe] No writable book in hotbar!");
            return;
        }

        // Переключение на книгу
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = bookSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(bookSlot));

        // Открытие интерфейса книги
        mc.player.networkHandler.sendPacket(new BookUpdateC2SPacket(bookSlot, List.of(""), java.util.Optional.of("Dupe")));

        mc.player.getInventory().selectedSlot = prevSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));

        timer.reset();
    }

    @Override
    public void onDisable() {
        stage = 0;
        targetPos = null;
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().name();
    }
}
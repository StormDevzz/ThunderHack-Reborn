package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ItemSelectSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;

import java.util.ArrayList;
import java.util.Random;

import static thunder.hack.features.modules.render.StorageEsp.getBlockEntities;

public class ChestStealer extends Module {
    public ChestStealer() {
        super("ChestStealer", Category.MISC);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Simple);

    // Simple + Steal
    public final Setting<ItemSelectSetting> items = new Setting<>("Items", new ItemSelectSetting(new ArrayList<>()),
            v -> mode.getValue() == Mode.Simple || mode.getValue() == Mode.Steal);
    private final Setting<Integer> delay = new Setting<>("Delay", 100, 0, 1000,
            v -> mode.getValue() == Mode.Simple || mode.getValue() == Mode.Steal || mode.getValue() == Mode.Dump);
    private final Setting<Boolean> random = new Setting<>("Random", false,
            v -> mode.getValue() == Mode.Simple);
    private final Setting<Boolean> close = new Setting<>("Close", false,
            v -> mode.getValue() == Mode.Simple);
    private final Setting<Sort> sort = new Setting<>("Sort", Sort.None,
            v -> mode.getValue() == Mode.Simple || mode.getValue() == Mode.Steal);

    // Button mode
    private final Setting<Integer> buttonStealDelay = new Setting<>("StealDelay", 0, 0, 500,
            v -> mode.getValue() == Mode.Button);
    private final Setting<Integer> buttonDumpDelay = new Setting<>("DumpDelay", 0, 0, 500,
            v -> mode.getValue() == Mode.Button);

    // Shared
    private final Setting<Boolean> autoMyst = new Setting<>("AutoMyst", false,
            v -> mode.getValue() == Mode.Simple || mode.getValue() == Mode.Steal);

    private final Timer autoMystDelay = new Timer();
    private final Timer timer = new Timer();
    private final Timer buttonTimer = new Timer();
    private final Random rnd = new Random();

    private int taskIndex = -1;
    private int taskEnd = -1;
    private int taskPhase = 0;
    private boolean taskIsSteal;

    public enum Mode {
        Simple, Button, Steal, Dump
    }

    public void startStealTask(ScreenHandler handler) {
        if (!(handler instanceof GenericContainerScreenHandler chest)) return;
        taskIsSteal = true;
        taskIndex = 0;
        taskEnd = chest.getInventory().size();
        if (!timer.every(10)) timer.reset();
    }

    public void startDumpTask(ScreenHandler handler) {
        if (!(handler instanceof GenericContainerScreenHandler chest)) return;
        taskIsSteal = false;
        taskPhase = 0;
        int chestSize = chest.slots.size() - 36;
        taskIndex = chestSize; // first inventory slot
        taskEnd = chestSize + 27; // after last inventory slot
        if (!timer.every(10)) timer.reset();
    }

    @Override
    public void onUpdate() {
        if (!(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler chest)) return;

        switch (mode.getValue()) {
            case Simple -> {
                for (int i = 0; i < chest.getInventory().size(); i++)
                    stealSlot(chest, i);
            }
            case Steal -> {
                for (int i = 0; i < chest.getInventory().size(); i++)
                    stealSlotFiltered(chest, i);
            }
            case Dump -> {
                int chestSize = chest.slots.size() - 36;
                dumpAll(chest, chestSize);
            }
            case Button -> processButtonTask(chest);
        }

        if (mode.getValue() != Mode.Button && isContainerEmpty(chest) && close.getValue())
            mc.player.closeHandledScreen();
    }

    private void processButtonTask(GenericContainerScreenHandler chest) {
        if (taskIndex < 0) return;

        int dl = taskIsSteal ? buttonStealDelay.getValue() : buttonDumpDelay.getValue();
        if (!buttonTimer.every(dl > 0 ? dl : 1)) return;

        if (taskIsSteal) {
            if (taskIndex >= taskEnd) {
                taskIndex = -1;
                return;
            }
            if (chest.getSlot(taskIndex).hasStack())
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, taskIndex, 0, SlotActionType.QUICK_MOVE, mc.player);
            taskIndex++;
        } else {
            if (taskIndex >= taskEnd) {
                if (taskPhase == 0) {
                    taskPhase = 1;
                    int chestSize = chest.slots.size() - 36;
                    taskIndex = chestSize + 27;
                    taskEnd = chestSize + 36;
                    return;
                } else {
                    taskIndex = -1;
                    return;
                }
            }
            if (chest.getSlot(taskIndex).hasStack())
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, taskIndex, 0, SlotActionType.QUICK_MOVE, mc.player);
            taskIndex++;
        }
    }

    private void dumpAll(GenericContainerScreenHandler chest, int chestSize) {
        if (!timer.every(delay.getValue())) return;
        // main inventory first, then hotbar
        for (int i = chestSize; i < chestSize + 27; i++)
            if (chest.getSlot(i).hasStack())
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
        for (int i = chestSize + 27; i < chestSize + 36; i++)
            if (chest.getSlot(i).hasStack())
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (autoMyst.getValue() && mc.currentScreen == null && autoMystDelay.passedMs(3000)) {
            for (BlockEntity be : getBlockEntities()) {
                if (be instanceof EnderChestBlockEntity) {
                    if (mc.player.squaredDistanceTo(be.getPos().toCenterPos()) > 39)
                        continue;
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(be.getPos().toCenterPos().add(MathUtility.random(-0.4, 0.4), 0.375, MathUtility.random(-0.4, 0.4)), Direction.UP, be.getPos(), false));
                    mc.player.swingHand(Hand.MAIN_HAND);
                    break;
                }
            }
        }
    }

    private void stealSlot(GenericContainerScreenHandler chest, int i) {
        if (!chest.getSlot(i).hasStack()) return;
        if (!isAllowed(chest.getSlot(i).getStack())) return;
        if (!timer.every(delay.getValue() + (random.getValue() && delay.getValue() != 0 ? rnd.nextInt(delay.getValue()) : 0))) return;
        if (mc.currentScreen != null && (mc.currentScreen.getTitle().getString().contains("Аукцион") || mc.currentScreen.getTitle().getString().contains("покупки"))) return;

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
        autoMystDelay.reset();
    }

    private void stealSlotFiltered(GenericContainerScreenHandler chest, int i) {
        if (!chest.getSlot(i).hasStack()) return;
        if (!isAllowed(chest.getSlot(i).getStack())) return;
        if (!timer.every(delay.getValue())) return;
        if (mc.currentScreen != null && (mc.currentScreen.getTitle().getString().contains("Аукцион") || mc.currentScreen.getTitle().getString().contains("покупки"))) return;

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
        autoMystDelay.reset();
    }

    private boolean isAllowed(ItemStack stack) {
        boolean allowed = items.getValue().contains(stack.getItem().getTranslationKey().replace("block.minecraft.", "").replace("item.minecraft.", ""));
        return switch (sort.getValue()) {
            case None -> true;
            case WhiteList -> allowed;
            default -> !allowed;
        };
    }

    private boolean isContainerEmpty(GenericContainerScreenHandler container) {
        int chestSize = container.slots.size() - 36;
        for (int i = 0; i < chestSize; i++)
            if (container.getSlot(i).hasStack()) return false;
        return true;
    }

    private enum Sort {None, WhiteList, BlackList}
}

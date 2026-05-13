package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EChestFarmer extends Module {
    private static final Setting<Pages> page = new Setting<>("Page", Pages.Main);

    /*   MAIN   */
    public final Setting<Float> range = new Setting<>("Range", 5.0f, 1.0f, 10.0f, v -> page.is(Pages.Main));
    public final Setting<Integer> delay = new Setting<>("Delay", 50, 0, 500, v -> page.is(Pages.Main));
    public final Setting<RotMode> rotate = new Setting<>("Rotate", RotMode.Client, v -> page.is(Pages.Main));
    public final Setting<Boolean> autoSwitch = new Setting<>("AutoSwitch", true, v -> page.is(Pages.Main));

    /*   PLACE   */
    public final Setting<Boolean> autoPlace = new Setting<>("AutoPlace", true, v -> page.is(Pages.Place));
    public final Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 200, 0, 1000, v -> page.is(Pages.Place) && autoPlace.getValue());
    public final Setting<Integer> distanceFromPlayer = new Setting<>("Distance", 2, 1, 5, v -> page.is(Pages.Place) && autoPlace.getValue());

    /*   RENDER   */
    public final Setting<Boolean> render = new Setting<>("Render", true, v -> page.is(Pages.Render));
    public final Setting<ColorSetting> fillColor = new Setting<>("Fill", new ColorSetting(HudEditor.getColor(0)), v -> page.is(Pages.Render) && render.getValue());
    public final Setting<ColorSetting> lineColor = new Setting<>("Line", new ColorSetting(HudEditor.getColor(0)), v -> page.is(Pages.Render) && render.getValue());
    public final Setting<Integer> lineWidth = new Setting<>("LineWidth", 2, 1, 5, v -> page.is(Pages.Render) && render.getValue());

    private final Timer actionTimer = new Timer();
    private final Timer placeTimer = new Timer();
    private BlockPos currentTarget;
    private int breakAttempts;
    private int totalActions;
    private Stage stage;
    private Vec3d targetVec;
    private float targetYaw, targetPitch;

    private enum Stage { FINDING, PLACING, BREAKING }
    public enum RotMode { Off, Client, Server }
    public enum Pages { Main, Place, Render }

    public EChestFarmer() {
        super("EChestFarmer", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        currentTarget = null;
        breakAttempts = 0;
        totalActions = 0;
        stage = Stage.FINDING;
        actionTimer.reset();
        placeTimer.reset();
        if (mc.options != null) mc.options.attackKey.setPressed(false);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.attackKey.setPressed(false);
        if (mc.interactionManager != null && currentTarget != null) {
            mc.interactionManager.cancelBlockBreaking();
        }
        currentTarget = null;
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent e) {
        if (mc.player == null || mc.world == null) return;

        switch (stage) {
            case FINDING -> handleFinding();
            case PLACING -> handlePlacing();
            case BREAKING -> handleBreaking();
        }
    }

    private void handleFinding() {
        currentTarget = findExistingEChest();
        if (currentTarget != null) {
            updateTargetRotation();
            stage = Stage.BREAKING;
            breakAttempts = 0;
            actionTimer.reset();
            return;
        }
        if (autoPlace.getValue()) {
            currentTarget = findPlacePosition();
            if (currentTarget != null) {
                updateTargetRotation();
                stage = Stage.PLACING;
                placeTimer.reset();
                return;
            }
        }
        // Не выключаемся, просто ждём
    }

    private void updateTargetRotation() {
        targetVec = currentTarget.toCenterPos();
        float[] angles = InteractionUtility.calculateAngle(targetVec);
        targetYaw = angles[0];
        targetPitch = angles[1];
    }

    private void applyRotation() {
        if (rotate.getValue() == RotMode.Client) {
            mc.player.setYaw(targetYaw);
            mc.player.setPitch(targetPitch);
        } else if (rotate.getValue() == RotMode.Server) {
            ModuleManager.rotations.fixRotation = targetYaw;
        }
    }

    private void handlePlacing() {
        if (!placeTimer.passedMs(placeDelay.getValue())) return;
        if (!isValidPlacePosition(currentTarget)) {
            stage = Stage.FINDING;
            return;
        }

        SearchInvResult chest = InventoryUtility.findItemInHotBar(Items.ENDER_CHEST);
        if (!chest.found()) {
            stage = Stage.FINDING;
            return;
        }

        applyRotation();

        int prevSlot = mc.player.getInventory().selectedSlot;
        if (autoSwitch.getValue()) chest.switchTo();

        boolean placed = InteractionUtility.placeBlock(currentTarget,
            InteractionUtility.Rotate.None, // Уже повернулись через applyRotation
            InteractionUtility.Interact.Strict,
            InteractionUtility.PlaceMode.Normal,
            true);

        if (autoSwitch.getValue()) InventoryUtility.switchTo(prevSlot);

        if (placed) {
            stage = Stage.BREAKING;
            breakAttempts = 0;
            actionTimer.reset();
        }
        placeTimer.reset();
    }

    private void handleBreaking() {
        if (!actionTimer.passedMs(delay.getValue())) return;
        if (!isEChest(currentTarget)) {
            mc.options.attackKey.setPressed(false);
            stage = Stage.FINDING;
            return;
        }
        if (mc.world.isAir(currentTarget)) {
            mc.options.attackKey.setPressed(false);
            stage = Stage.FINDING;
            return;
        }

        applyRotation();

        // Switch to pickaxe
        int prevSlot = mc.player.getInventory().selectedSlot;
        if (autoSwitch.getValue()) {
            SearchInvResult pick = InventoryUtility.findItemInHotBar(
                Items.NETHERITE_PICKAXE, Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE,
                Items.STONE_PICKAXE, Items.WOODEN_PICKAXE, Items.GOLDEN_PICKAXE
            );
            if (pick.found()) pick.switchTo();
        }

        // Ванильное ломание: зажимаем кнопку + отправляем START_DESTROY_BLOCK
        mc.options.attackKey.setPressed(true);
        mc.interactionManager.attackBlock(currentTarget, Direction.UP);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (autoSwitch.getValue()) InventoryUtility.switchTo(prevSlot);

        breakAttempts++;
        totalActions++;

        if (mc.world.isAir(currentTarget)) {
            mc.options.attackKey.setPressed(false);
            stage = Stage.FINDING; // Ищем следующий
        }

        actionTimer.reset();
    }

    private boolean isEChest(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.ENDER_CHEST;
    }

    private boolean isValidPlacePosition(BlockPos pos) {
        if (pos == null) return false;
        BlockPos below = pos.down();
        return mc.world.getBlockState(below).isSolid() && mc.world.getBlockState(pos).isReplaceable();
    }

    private BlockPos findExistingEChest() {
        BlockPos playerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.getValue());
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int x = -r; x <= r; x++)
            for (int y = -r; y <= r; y++)
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (isEChest(pos)) {
                        double dist = mc.player.squaredDistanceTo(pos.toCenterPos());
                        if (dist < bestDist) {
                            bestDist = dist;
                            best = pos.toImmutable();
                        }
                    }
                }
        return best;
    }

    private BlockPos findPlacePosition() {
        BlockPos playerPos = mc.player.getBlockPos();
        int dist = distanceFromPlayer.getValue();

        // Проверяем все 4 стороны на точном расстоянии
        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction dir : dirs) {
            BlockPos pos = playerPos.offset(dir, dist);
            if (isValidPlacePosition(pos)) return pos.toImmutable();
        }

        // Если точное расстояние не подходит — ищем в квадрате dist x dist вокруг
        for (int x = -dist; x <= dist; x++)
            for (int z = -dist; z <= dist; z++) {
                if (Math.abs(x) != dist && Math.abs(z) != dist) continue; // Только периметр
                BlockPos pos = playerPos.add(x, 0, z);
                if (isValidPlacePosition(pos)) return pos.toImmutable();
            }

        return null;
    }

    @Override
    public String getDisplayInfo() {
        return stage + (currentTarget != null ? " " + breakAttempts : "");
    }

    public void onRender3D(MatrixStack stack) {
        if (!render.getValue() || currentTarget == null) return;
        if (stage != Stage.BREAKING && stage != Stage.PLACING) return;

        Box box = new Box(currentTarget);
        Color fill = fillColor.getValue().getColorObject();
        Color line = lineColor.getValue().getColorObject();

        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(box, fill));
        Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(box, line, lineWidth.getValue()));
    }
}
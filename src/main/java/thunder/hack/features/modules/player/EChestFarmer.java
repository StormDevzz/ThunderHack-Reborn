package thunder.hack.features.modules.player;

// ThunderHack Plus by VFedTerV & Xiaofeng
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
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

    /*   BREAK   */
    public final Setting<Boolean> packetMine = new Setting<>("PacketMine", false, v -> page.is(Pages.Break));
    public final Setting<Integer> startDelay = new Setting<>("StartDelay", 2, 0, 10, v -> page.is(Pages.Break) && !packetMine.getValue());
    public final Setting<Integer> resetThreshold = new Setting<>("ResetThreshold", 20, 5, 50, v -> page.is(Pages.Break) && !packetMine.getValue());

    /*   RENDER   */
    public final Setting<Boolean> render = new Setting<>("Render", true, v -> page.is(Pages.Render));
    public final Setting<ColorSetting> fillColor = new Setting<>("Fill", new ColorSetting(HudEditor.getColor(0)), v -> page.is(Pages.Render) && render.getValue());
    public final Setting<ColorSetting> lineColor = new Setting<>("Line", new ColorSetting(HudEditor.getColor(0)), v -> page.is(Pages.Render) && render.getValue());
    public final Setting<Integer> lineWidth = new Setting<>("LineWidth", 2, 1, 5, v -> page.is(Pages.Render) && render.getValue());

    /*   MISC   */
    public final Setting<Boolean> autoDisable = new Setting<>("AutoDisable", false, v -> page.is(Pages.Misc));

    private final Timer actionTimer = new Timer();
    private final Timer placeTimer = new Timer();

    // 新增: 多个末影箱目标列表 (支持批量破坏)
    private List<BlockPos> targets = new ArrayList<>();
    // 新增: 当前正在处理的目标索引
    private int currentTargetIndex = 0;

    private int breakAttempts;
    private Stage stage;
    private boolean mining;
    private int startDelayCounter;
    private float targetYaw, targetPitch;

    private enum Stage { FINDING, PLACING, BREAKING }
    public enum RotMode { Off, Client, Server }
    public enum Pages { Main, Place, Break, Render, Misc }

    public EChestFarmer() {
        super("EChestFarmer", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        targets.clear();
        currentTargetIndex = 0;
        breakAttempts = 0;
        stage = Stage.FINDING;
        mining = false;
        startDelayCounter = 0;
        actionTimer.reset();
        placeTimer.reset();
        if (mc.options != null) mc.options.attackKey.setPressed(false);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.attackKey.setPressed(false);
        if (mc.interactionManager != null && getCurrentTarget() != null) {
            mc.interactionManager.cancelBlockBreaking();
        }
        targets.clear();
        mining = false;
    }

    // 新增: 获取当前目标 (支持多目标)
    private BlockPos getCurrentTarget() {
        if (targets.isEmpty() || currentTargetIndex >= targets.size()) return null;
        return targets.get(currentTargetIndex);
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

    private void updateRotation() {
        BlockPos target = getCurrentTarget();
        if (target == null) return;
        Vec3d targetVec = target.toCenterPos();
        float[] angles = InteractionUtility.calculateAngle(targetVec);
        targetYaw = angles[0];
        targetPitch = angles[1];

        if (rotate.getValue() == RotMode.Client) {
            mc.player.setYaw(targetYaw);
            mc.player.setPitch(targetPitch);
        } else if (rotate.getValue() == RotMode.Server) {
            mc.player.setYaw(targetYaw);
            mc.player.setPitch(targetPitch);
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(targetYaw, targetPitch, mc.player.isOnGround()));
        }
    }

    private void handleFinding() {
        // 改进: 查找范围内所有末影箱 (支持多个)
        List<BlockPos> existing = findAllEChests();
        if (!existing.isEmpty()) {
            targets = existing;
            currentTargetIndex = 0;
            updateRotation();
            stage = Stage.BREAKING;
            breakAttempts = 0;
            mining = false;
            startDelayCounter = 0;
            actionTimer.reset();
            return;
        }

        if (autoPlace.getValue()) {
            // 改进: 使用智能优先级选择最佳放置位置
            BlockPos bestPos = findBestPlacePosition();
            if (bestPos != null) {
                targets.clear();
                targets.add(bestPos);
                currentTargetIndex = 0;
                updateRotation();
                stage = Stage.PLACING;
                placeTimer.reset();
                return;
            }
        }

        if (autoDisable.getValue()) {
            disable();
        }
    }

    private void handlePlacing() {
        BlockPos target = getCurrentTarget();
        if (!placeTimer.passedMs(placeDelay.getValue())) return;
        if (target == null || !isValidPlacePosition(target)) {
            stage = Stage.FINDING;
            return;
        }

        SearchInvResult chest = InventoryUtility.findItemInHotBar(Items.ENDER_CHEST);
        if (!chest.found()) {
            if (autoDisable.getValue()) disable();
            else stage = Stage.FINDING;
            return;
        }

        updateRotation();

        int prevSlot = mc.player.getInventory().selectedSlot;
        if (autoSwitch.getValue()) chest.switchTo();

        boolean placed = InteractionUtility.placeBlock(target,
                rotate.getValue() != RotMode.Off ? InteractionUtility.Rotate.Default : InteractionUtility.Rotate.None,
                InteractionUtility.Interact.Strict,
                InteractionUtility.PlaceMode.Normal,
                true);

        if (autoSwitch.getValue()) InventoryUtility.switchTo(prevSlot);

        if (placed) {
            stage = Stage.BREAKING;
            breakAttempts = 0;
            mining = false;
            startDelayCounter = 0;
            actionTimer.reset();
        } else {
            stage = Stage.FINDING;
        }
        placeTimer.reset();
    }

    private void handleBreaking() {
        BlockPos target = getCurrentTarget();
        if (!actionTimer.passedMs(delay.getValue())) return;

        // 新增: 延迟/中断处理 - 如果目标无效则跳过
        if (target == null || !isEChest(target) || mc.world.isAir(target)) {
            stopMining();
            moveToNextTarget();
            return;
        }

        // 新增: 检查方块是否被其他玩家破坏中
        if (mc.world.getBlockState(target).getHardness(mc.world, target) == -1) {
            moveToNextTarget();
            return;
        }

        updateRotation();

        int prevSlot = mc.player.getInventory().selectedSlot;
        if (autoSwitch.getValue()) {
            SearchInvResult pick = InventoryUtility.findItemInHotBar(
                    Items.NETHERITE_PICKAXE, Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE,
                    Items.STONE_PICKAXE, Items.WOODEN_PICKAXE, Items.GOLDEN_PICKAXE
            );
            if (pick.found()) pick.switchTo();
        }

        if (packetMine.getValue()) {
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target, Direction.UP));
            sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target, Direction.UP));
            mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            if (!mining) {
                if (startDelayCounter < startDelay.getValue()) {
                    startDelayCounter++;
                    if (autoSwitch.getValue()) InventoryUtility.switchTo(prevSlot);
                    actionTimer.reset();
                    return;
                }
                mining = true;
                mc.options.attackKey.setPressed(true);
                mc.interactionManager.attackBlock(target, Direction.UP);
            } else {
                breakAttempts++;
                if (breakAttempts >= resetThreshold.getValue()) {
                    stopMining();
                    startDelayCounter = 0;
                } else {
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            }
        }

        if (autoSwitch.getValue()) InventoryUtility.switchTo(prevSlot);

        if (mc.world.isAir(target)) {
            stopMining();
            moveToNextTarget();
        }

        actionTimer.reset();
    }

    // 新增: 切换到下一个目标 (支持批量破坏)
    private void moveToNextTarget() {
        stopMining();
        if (targets.isEmpty()) {
            stage = Stage.FINDING;
            return;
        }
        currentTargetIndex++;
        if (currentTargetIndex >= targets.size()) {
            if (autoDisable.getValue()) {
                disable();
            } else {
                targets.clear();
                stage = Stage.FINDING;
            }
        } else {
            breakAttempts = 0;
            mining = false;
            startDelayCounter = 0;
            actionTimer.reset();
            stage = Stage.BREAKING;
        }
    }

    private void stopMining() {
        mc.options.attackKey.setPressed(false);
        BlockPos target = getCurrentTarget();
        if (mc.interactionManager != null && target != null) {
            mc.interactionManager.cancelBlockBreaking();
        }
        mining = false;
        breakAttempts = 0;
    }

    private boolean isEChest(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.ENDER_CHEST;
    }

    private boolean isValidPlacePosition(BlockPos pos) {
        if (pos == null) return false;
        BlockPos below = pos.down();
        return mc.world.getBlockState(below).isSolid() && mc.world.getBlockState(pos).isReplaceable();
    }

    // 改进: 查找范围内所有末影箱 (支持多目标)
    private List<BlockPos> findAllEChests() {
        List<BlockPos> found = new ArrayList<>();
        BlockPos playerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.getValue());
        for (int x = -r; x <= r; x++)
            for (int y = -r; y <= r; y++)
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (isEChest(pos)) {
                        found.add(pos.toImmutable());
                    }
                }
        found.sort(Comparator.comparingDouble(p -> mc.player.squaredDistanceTo(p.toCenterPos())));
        return found;
    }

    // 改进: 智能优先级选择最佳放置位置 (安全性 + 可见性)
    private BlockPos findBestPlacePosition() {
        BlockPos playerPos = mc.player.getBlockPos();
        int dist = distanceFromPlayer.getValue();
        List<BlockPos> candidates = new ArrayList<>();

        Direction[] dirs = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        for (Direction dir : dirs) {
            BlockPos pos = playerPos.offset(dir, dist);
            if (isValidPlacePosition(pos)) candidates.add(pos.toImmutable());
        }

        for (int x = -dist; x <= dist; x++)
            for (int z = -dist; z <= dist; z++) {
                BlockPos pos = playerPos.add(x, 0, z);
                if (isValidPlacePosition(pos) && !candidates.contains(pos)) candidates.add(pos.toImmutable());
            }

        if (candidates.isEmpty()) return null;

        candidates.sort((a, b) -> {
            int scoreA = evaluatePositionSafety(a);
            int scoreB = evaluatePositionSafety(b);
            if (scoreA != scoreB) return Integer.compare(scoreB, scoreA);
            return Double.compare(mc.player.squaredDistanceTo(a.toCenterPos()), mc.player.squaredDistanceTo(b.toCenterPos()));
        });

        return candidates.get(0);
    }

    // 新增: 评估位置安全性 (避免熔岩、仙人掌、水、基岩等危险)
    private int evaluatePositionSafety(BlockPos pos) {
        int score = 0;

        // 检查周围方块是否有危险
        BlockPos[] dangerous = {
                pos.up(), pos.north(), pos.south(), pos.east(), pos.west(), pos.down()
        };
        for (BlockPos neighbor : dangerous) {
            if (mc.world.getBlockState(neighbor).getBlock() == Blocks.LAVA) score -= 5;
            if (mc.world.getBlockState(neighbor).getBlock() == Blocks.CACTUS) score -= 10;
            if (mc.world.getBlockState(neighbor).getBlock() == Blocks.WATER) score -= 2;
            if (!mc.world.isAir(neighbor) && mc.world.getBlockState(neighbor).getBlock().getHardness() == -1) score -= 3;
        }

        // 检查上方是否有阻挡 (简单可靠)
        if (!mc.world.isAir(pos.up())) score -= 1;

        // 距离越近越好
        double distance = mc.player.squaredDistanceTo(pos.toCenterPos());
        if (distance < 4) score += 2;

        return score;
    }

    @Override
    public String getDisplayInfo() {
        BlockPos cur = getCurrentTarget();
        return stage + (cur != null ? " " + breakAttempts : "");
    }

    public void onRender3D(MatrixStack stack) {
        if (!render.getValue()) return;
        if (stage != Stage.BREAKING && stage != Stage.PLACING) return;
        BlockPos target = getCurrentTarget();
        if (target == null) return;

        Box box = new Box(target);
        Color fill = fillColor.getValue().getColorObject();
        Color line = lineColor.getValue().getColorObject();

        Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(box, fill));
        Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(box, line, lineWidth.getValue()));
    }
}
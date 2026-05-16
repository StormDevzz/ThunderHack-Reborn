package thunder.hack.features.modules.misc;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InventoryUtility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoFarm extends Module {
    public AutoFarm() {
        super("AutoFarm", Category.MISC);
    }

    // Общие настройки
    public final Setting<Float> range = new Setting<>("Range", 4.5f, 2f, 7f);

    // Группа "Crops"
    public final Setting<SettingGroup> cropsGroup = new Setting<>("Crops", new SettingGroup(false, 0));
    public final Setting<Crops> crops = new Setting<>("Crops", Crops.All).addToGroup(cropsGroup);

    // Группа "Swaps"
    public final Setting<SettingGroup> swapsGroup = new Setting<>("Swaps", new SettingGroup(false, 0));
    public final Setting<SwapMode> swapMode = new Setting<>("SwapMode", SwapMode.Normal).addToGroup(swapsGroup);
    public final Setting<Boolean> useHoe = new Setting<>("UseHoe", false).addToGroup(swapsGroup);

    // Группа "Rotations"
    public final Setting<SettingGroup> rotationsGroup = new Setting<>("Rotations", new SettingGroup(false, 0));
    public final Setting<RotationMode> rotationMode = new Setting<>("Mode", RotationMode.None)
            .addToGroup(rotationsGroup);

    // Группа "Break"
    public final Setting<SettingGroup> breakGroup = new Setting<>("Break", new SettingGroup(false, 0));
    public final Setting<Integer> breakDelay = new Setting<>("BreakDelay", 50, 0, 500).addToGroup(breakGroup);

    private enum SwapMode {
        Normal, Silent, None
    }

    private enum Crops {
        All, Wheat, Carrots, Potatoes, Beetroots, NetherWart, Cocoa
    }

    private enum RotationMode {
        Client, Server, None
    }

    private final Timer breakTimer = new Timer();

    @Override
    public void onEnable() {
        breakTimer.reset();
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent e) {
        // Только сбор урожая
        BlockPos target = findCrop();
        if (target != null && breakTimer.passedMs(breakDelay.getValue())) {
            harvest(target);
            breakTimer.reset();
        }
    }

    private void harvest(BlockPos target) {
        rotateToBlock(target);

        int prevSlot = mc.player.getInventory().selectedSlot;
        boolean swapped = false;

        // Используем мотыгу для мгновенного сбора
        if (useHoe.getValue()) {
            int toolSlot = getBestHoeSlot();
            if (toolSlot != -1) {
                swapTo(toolSlot);
                swapped = true;
            }
        }

        // Ломаем блок
        mc.player.networkHandler.sendPacket(
                new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target, Direction.UP));
        mc.player.networkHandler.sendPacket(
                new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target, Direction.UP));
        mc.player.swingHand(Hand.MAIN_HAND);

        if (swapped)
            swapBack(prevSlot);
    }

    private void rotateToBlock(BlockPos pos) {
        if (rotationMode.getValue() == RotationMode.None)
            return;

        Vec3d targetVec = pos.toCenterPos();
        double dx = targetVec.x - mc.player.getX();
        double dy = targetVec.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double dz = targetVec.z - mc.player.getZ();
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));

        if (rotationMode.getValue() == RotationMode.Client) {
            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        } else if (rotationMode.getValue() == RotationMode.Server) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    yaw, pitch, mc.player.isOnGround()));
        }
    }

    private BlockPos findCrop() {
        List<BlockPos> cropList = new ArrayList<>();
        int r = (int) Math.ceil(range.getValue());

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = mc.player.getBlockPos().add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    if (isMatureCrop(state.getBlock(), state)) {
                        cropList.add(pos);
                    }
                }
            }
        }

        return cropList.stream()
                .min(Comparator.comparingDouble(p -> p.getSquaredDistance(mc.player.getPos())))
                .orElse(null);
    }

    private boolean isMatureCrop(Block block, BlockState state) {
        return switch (crops.getValue()) {
            case All -> (block instanceof CropBlock crop && crop.isMature(state))
                    || (block instanceof NetherWartBlock && state.get(NetherWartBlock.AGE) >= 3)
                    || (block instanceof CocoaBlock && state.get(CocoaBlock.AGE) >= 2);
            case Wheat -> block == Blocks.WHEAT && ((CropBlock) block).isMature(state);
            case Carrots -> block == Blocks.CARROTS && ((CropBlock) block).isMature(state);
            case Potatoes -> block == Blocks.POTATOES && ((CropBlock) block).isMature(state);
            case Beetroots -> block instanceof BeetrootsBlock && state.get(BeetrootsBlock.AGE) >= 3;
            case NetherWart -> block instanceof NetherWartBlock && state.get(NetherWartBlock.AGE) >= 3;
            case Cocoa -> block instanceof CocoaBlock && state.get(CocoaBlock.AGE) >= 2;
        };
    }

    private void swapTo(int slot) {
        if (swapMode.getValue() == SwapMode.Silent) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        } else if (swapMode.getValue() == SwapMode.Normal) {
            mc.player.getInventory().selectedSlot = slot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        }
    }

    private void swapBack(int slot) {
        if (swapMode.getValue() != SwapMode.None) {
            if (swapMode.getValue() == SwapMode.Silent) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            } else {
                mc.player.getInventory().selectedSlot = slot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            }
        }
    }

    private int getBestHoeSlot() {
        return InventoryUtility.findItemInHotBar(
                Items.NETHERITE_HOE, Items.DIAMOND_HOE, Items.IRON_HOE,
                Items.GOLDEN_HOE, Items.STONE_HOE, Items.WOODEN_HOE).slot();
    }

    @Override
    public String getDisplayInfo() {
        return crops.getValue().name();
    }
}
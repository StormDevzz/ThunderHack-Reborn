package thunder.hack.features.modules.player;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.WorldChunk;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.Timer;

import java.util.ArrayList;
import java.util.List;

public class GhostHand extends Module {
    public GhostHand() {
        super("GhostHand", Category.PLAYER);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Normal);
    private final Setting<Float> range = new Setting<>("Range", 5f, 1f, 12f);
    private final Setting<Boolean> throughWalls = new Setting<>("ThroughWalls", true, v -> mode.is(Mode.Normal));
    private final Setting<Integer> cooldown = new Setting<>("Cooldown", 200, 0, 1000);
    private final Setting<Boolean> noSwing = new Setting<>("NoSwing", false);

    private final Setting<SettingGroup> containerGroup = new Setting<>("Containers", new SettingGroup(false, 0));
    public final Setting<Boolean> chest = new Setting<>("Chest", true).addToGroup(containerGroup);
    public final Setting<Boolean> echest = new Setting<>("EnderChest", true).addToGroup(containerGroup);
    public final Setting<Boolean> barrel = new Setting<>("Barrel", true).addToGroup(containerGroup);
    public final Setting<Boolean> shulker = new Setting<>("Shulker", true).addToGroup(containerGroup);
    public final Setting<Boolean> furnace = new Setting<>("Furnace", true).addToGroup(containerGroup);
    public final Setting<Boolean> dispenser = new Setting<>("Dispenser", true).addToGroup(containerGroup);
    public final Setting<Boolean> hopper = new Setting<>("Hopper", true).addToGroup(containerGroup);
    public final Setting<Boolean> trappedChest = new Setting<>("TrappedChest", true).addToGroup(containerGroup);

    private final Timer clickTimer = new Timer();

    public enum Mode {
        Normal, Always
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent e) {
        if (!clickTimer.passedMs(cooldown.getValue())) return;
        if (!mc.options.useKey.isPressed()) return;
        if (mc.player.isUsingItem()) return;

        HitResult hit = mc.crosshairTarget;

        if (mode.is(Mode.Always)) {
            BlockEntity be = findClosestContainer();
            if (be != null) {
                interact(be);
                clickTimer.reset();
            }
            return;
        }

        if (hit instanceof BlockHitResult bhr) {
            if (mc.world.getBlockState(bhr.getBlockPos()).isAir()) return;

            if (isContainerBlock(bhr.getBlockPos())) return;

            if (throughWalls.getValue()) {
                BlockEntity be = findContainerThroughBlock(bhr.getBlockPos());
                if (be != null) {
                    interact(be);
                    clickTimer.reset();
                }
            }
        }
    }

    private void interact(BlockEntity be) {
        BlockHitResult bhr = new BlockHitResult(
                be.getPos().toCenterPos(),
                Direction.UP,
                be.getPos(),
                false
        );
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        if (!noSwing.getValue()) mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean isContainerBlock(BlockPos pos) {
        BlockEntity be = mc.world.getBlockEntity(pos);
        if (be == null) return false;
        return isContainer(be);
    }

    private BlockEntity findContainerThroughBlock(BlockPos wallPos) {
        ClientPlayerEntity p = mc.player;
        Vec3d eyePos = p.getEyePos();
        Vec3d lookVec = p.getRotationVec(1f);

        BlockEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (BlockEntity be : getBlockEntities()) {
            if (!isContainer(be)) continue;
            if (be.getPos().equals(wallPos)) continue;

            double dist = be.getPos().getSquaredDistance(wallPos);
            if (dist > range.getValue() * range.getValue()) continue;

            Vec3d to = be.getPos().toCenterPos().subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(to);
            if (dot < 0.9) continue;

            double distToPlayer = eyePos.squaredDistanceTo(be.getPos().toCenterPos());
            if (distToPlayer < closestDist) {
                closestDist = distToPlayer;
                closest = be;
            }
        }
        return closest;
    }

    private BlockEntity findClosestContainer() {
        ClientPlayerEntity p = mc.player;
        Vec3d eyePos = p.getEyePos();
        Vec3d lookVec = p.getRotationVec(1f);

        BlockEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (BlockEntity be : getBlockEntities()) {
            if (!isContainer(be)) continue;

            Vec3d to = be.getPos().toCenterPos().subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(to);
            if (dot < 0.9) continue;

            double dist = eyePos.squaredDistanceTo(be.getPos().toCenterPos());
            if (dist > range.getValue() * range.getValue()) continue;

            if (dist < closestDist) {
                closestDist = dist;
                closest = be;
            }
        }
        return closest;
    }

    private boolean isContainer(BlockEntity be) {
        if (be instanceof ChestBlockEntity && chest.getValue()) return true;
        if (be instanceof TrappedChestBlockEntity && trappedChest.getValue()) return true;
        if (be instanceof EnderChestBlockEntity && echest.getValue()) return true;
        if (be instanceof BarrelBlockEntity && barrel.getValue()) return true;
        if (be instanceof ShulkerBoxBlockEntity && shulker.getValue()) return true;
        if (be instanceof AbstractFurnaceBlockEntity && furnace.getValue()) return true;
        if (be instanceof DispenserBlockEntity && dispenser.getValue()) return true;
        if (be instanceof HopperBlockEntity && hopper.getValue()) return true;
        return false;
    }

    private List<BlockEntity> getBlockEntities() {
        List<BlockEntity> list = new ArrayList<>();
        int viewDist = mc.options.getViewDistance().getValue();
        int playerX = (int) mc.player.getX() / 16;
        int playerZ = (int) mc.player.getZ() / 16;
        for (int x = -viewDist; x <= viewDist; x++) {
            for (int z = -viewDist; z <= viewDist; z++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(playerX + x, playerZ + z);
                if (chunk != null)
                    list.addAll(chunk.getBlockEntities().values());
            }
        }
        return list;
    }
}

package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.EventSync;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class Spider extends Module {
    public final Setting<Integer> delay = new Setting<>("delay", 2, 1, 15);
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Matrix);

    // Fast settings (original + extra)
    private final Setting<Float> fastSpeed = new Setting<>("FastSpeed", 0.42f, 0.1f, 1.5f, v -> mode.getValue() == Mode.Fast);
    private final Setting<Boolean> fastInstant = new Setting<>("FastInstant", true, v -> mode.getValue() == Mode.Fast);
    private final Setting<Boolean> fastOnlyCollision = new Setting<>("FastOnlyCollision", true, v -> mode.getValue() == Mode.Fast);
    private final Setting<Boolean> fastRequireJump = new Setting<>("FastRequireJump", true, v -> mode.getValue() == Mode.Fast);
    private final Setting<Boolean> fastGroundSpoof = new Setting<>("FastGroundSpoof", false, v -> mode.getValue() == Mode.Fast && !fastInstant.getValue());
    private final Setting<Integer> fastDelay = new Setting<>("FastDelay", -1, -1, 20, v -> mode.getValue() == Mode.Fast);

    // Custom mode (10 settings)
    private final Setting<Float> customMotionY = new Setting<>("CustomMotionY", 0.42f, 0f, 2f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Integer> customDelay = new Setting<>("CustomDelay", 2, 1, 20, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> customRequireJump = new Setting<>("CustomRequireJump", true, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> customOnlyCollision = new Setting<>("CustomOnlyCollision", true, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> customGroundSpoof = new Setting<>("CustomGroundSpoof", true, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> customMatrixSpoof = new Setting<>("CustomMatrixSpoof", false, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> customJump = new Setting<>("CustomJump", true, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> customUseTimer = new Setting<>("CustomUseTimer", false, v -> mode.getValue() == Mode.Custom);
    private final Setting<Float> customTimer = new Setting<>("CustomTimer", 1.0f, 0.1f, 2.0f, v -> mode.getValue() == Mode.Custom && customUseTimer.getValue());
    private final Setting<Boolean> customResetMotion = new Setting<>("CustomResetMotion", false, v -> mode.getValue() == Mode.Custom);

    public Spider() {
        super("Spider", Category.MOVEMENT);
    }

    public static Direction getPlaceableSide(BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbour = pos.offset(side);
            if (mc.world.isAir(neighbour)) {
                continue;
            }
            if (!mc.world.getBlockState(neighbour).isReplaceable()) {
                return side;
            }
        }
        return null;
    }

    @Override
    public void onUpdate() {
        Mode currentMode = mode.getValue();

        if (currentMode == Mode.FunTime) {
            if (!mc.player.horizontalCollision) return;
            if (mc.player.age % 2 == 0 && mc.options.jumpKey.isPressed()) {
                float pitch = mc.player.getPitch();
                mc.player.setPitch(82);
                int slot = getAtHotBar();
                if (slot != -1) {
                    int originalSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = slot;
                    sendPacket(new UpdateSelectedSlotC2SPacket(slot));

                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    mc.player.swingHand(Hand.MAIN_HAND);

                    mc.player.getInventory().selectedSlot = originalSlot;
                    sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
                }
                mc.player.setPitch(pitch);
            }
        } else if (currentMode == Mode.Default) {
            if (!mc.player.horizontalCollision) return;
            mc.player.setVelocity(mc.player.getVelocity().getX(), 0.21, mc.player.getVelocity().getZ());
        } else if (currentMode == Mode.Matrix) {
            if (!mc.player.horizontalCollision) return;
            mc.player.setOnGround(mc.player.age % delay.getValue() == 0);
            mc.player.prevY -= 2.0E-232;
            if (mc.player.isOnGround())
                mc.player.setVelocity(mc.player.getVelocity().getX(), 0.42, mc.player.getVelocity().getZ());
        } else if (currentMode == Mode.Vanilla) {
            if (!mc.player.horizontalCollision) return;
            if (mc.options.jumpKey.isPressed() && mc.player.age % delay.getValue() == 0) {
                mc.player.setOnGround(true);
                mc.player.jump();
            }
        } else if (currentMode == Mode.Fast) {
            if (fastOnlyCollision.getValue() && !mc.player.horizontalCollision) return;
            if (fastRequireJump.getValue() && !mc.options.jumpKey.isPressed()) return;
            int useDelay = fastDelay.getValue() != -1 ? fastDelay.getValue() : delay.getValue();
            if (mc.player.age % useDelay == 0) {
                if (fastInstant.getValue()) {
                    mc.player.setOnGround(true);
                    mc.player.jump();
                    mc.player.setVelocity(mc.player.getVelocity().getX(), fastSpeed.getValue(), mc.player.getVelocity().getZ());
                } else {
                    if (fastGroundSpoof.getValue()) mc.player.setOnGround(true);
                    mc.player.setVelocity(mc.player.getVelocity().getX(), fastSpeed.getValue(), mc.player.getVelocity().getZ());
                }
            }
        } else if (currentMode == Mode.Custom) {
            if (customOnlyCollision.getValue() && !mc.player.horizontalCollision) return;
            if (customRequireJump.getValue() && !mc.options.jumpKey.isPressed()) return;
            if (mc.player.age % customDelay.getValue() != 0) return;

            if (customGroundSpoof.getValue()) mc.player.setOnGround(true);
            if (customMatrixSpoof.getValue()) mc.player.prevY -= 2.0E-232;
            if (customJump.getValue()) mc.player.jump();
            if (customMotionY.getValue() > 0) {
                if (customResetMotion.getValue()) {
                    mc.player.setVelocity(0, customMotionY.getValue(), 0);
                } else {
                    mc.player.setVelocity(mc.player.getVelocity().x, customMotionY.getValue(), mc.player.getVelocity().z);
                }
            }
            if (customUseTimer.getValue()) {
                mc.player.getWorld().getTickManager().setTickRate(customTimer.getValue() * 20.0f);
            }
        }
    }

    @EventHandler
    public void onSync(EventSync event) {
        if (mc.options.jumpKey.isPressed() && mc.player.getVelocity().getY() <= -0.3739040364667221 && mode.getValue() == Mode.MatrixNew) {
            mc.player.setOnGround(true);
            mc.player.setVelocity(mc.player.getVelocity().getX(), 0.481145141919180, mc.player.getVelocity().getZ());
        }
        if (mc.player.age % delay.getValue() == 0 && mc.player.horizontalCollision && MovementUtility.isMoving() && mode.getValue() == Mode.Blocks) {
            int find = -2;
            for (int i = 0; i <= 8; i++)
                if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) find = i;
            if (find == -2) return;
            BlockPos pos = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());
            Direction side = getPlaceableSide(pos);
            if (side != null) {
                sendPacket(new UpdateSelectedSlotC2SPacket(find));
                BlockPos neighbour = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ()).offset(side);
                Direction opposite = side.getOpposite();
                Vec3d hitVec = new Vec3d(neighbour.getX() + 0.5, neighbour.getY() + 0.5, neighbour.getZ() + 0.5).add(new Vec3d(opposite.getUnitVector()).multiply(0.5));
                sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(hitVec, opposite, neighbour, false), id));
                sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                if (mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos()).add(0, 2, 0)).getBlock() != Blocks.AIR) {
                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, neighbour, opposite));
                    sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, neighbour, opposite));
                }
                sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            mc.player.setOnGround(true);
            mc.player.jump();
            sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }
    }

    @Override
    public void onDisable() {
        if (mode.getValue() == Mode.Custom && customUseTimer.getValue()) {
            mc.player.getWorld().getTickManager().setTickRate(20.0f);
        }
    }

    private int getAtHotBar() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!(itemStack.getItem() == Items.WATER_BUCKET)) continue;
            return i;
        }
        return -1;
    }

    public enum Mode {
        Default, Matrix, MatrixNew, Blocks, FunTime, Vanilla, Fast, Custom
    }
}
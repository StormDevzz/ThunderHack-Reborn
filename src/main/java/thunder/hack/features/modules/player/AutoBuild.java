package thunder.hack.features.modules.player;

import baritone.api.BaritoneAPI;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;

import java.util.HashSet;

public final class AutoBuild extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Portal);
    public final Setting<BuildMode> buildMode = new Setting<>("BuildMode", BuildMode.Simple, v -> mode.is(Mode.Highway));
    public final Setting<Integer> distance = new Setting<>("Distance", 3, 1, 6);
    public final Setting<Integer> delayBetween = new Setting<>("Delay", 2, 0, 10);
    public final Setting<Integer> highwayWidth = new Setting<>("Width", 3, 1, 7, v -> mode.is(Mode.Highway));
    public final Setting<InteractionUtility.Rotate> rotate = new Setting<>("Rotate", InteractionUtility.Rotate.Default);
    public final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.Normal);

    private final Timer timer = new Timer();
    private BlockPos portalStart;
    private int portalIndex;
    private boolean building;
    private boolean baritoneStarted;
    private boolean hasBaritone = true;
    private final HashSet<BlockPos> builtPositions = new HashSet<>();

    public AutoBuild() {
        super("AutoBuild", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        reset();
        baritoneStarted = false;
    }

    @Override
    public void onDisable() {
        builtPositions.clear();
        stopBaritone();
    }

    private void reset() {
        portalStart = null;
        portalIndex = 0;
        building = false;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        if (!timer.passedMs(delayBetween.getValue() * 50L)) return;

        SearchInvResult obby = InventoryUtility.findBlockInHotBar(Blocks.OBSIDIAN);
        if (!obby.found()) return;

        switch (mode.getValue()) {
            case Portal -> updatePortal(obby.slot());
            case Highway -> updateHighway(obby.slot());
        }

        timer.reset();
    }

    private void updatePortal(int slot) {
        if (!building) {
            Direction facing = mc.player.getHorizontalFacing();
            Direction right = facing.rotateYClockwise();
            portalStart = BlockPos.ofFloored(mc.player.getPos())
                    .offset(facing, distance.getValue())
                    .offset(right, -1);
            building = true;
        }

        int total = 4 * 5 - (4 - 2) * (5 - 2);
        if (portalIndex >= total) { disable(); return; }

        while (portalIndex < total) {
            int x = portalIndex % 4;
            int y = portalIndex / 4;
            portalIndex++;

            if (x == 0 || x == 3 || y == 0 || y == 4) {
                BlockPos pos = portalStart.add(x, y, 0);
                if (mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN) continue;
                if (!InteractionUtility.canPlaceBlock(pos, InteractionUtility.Interact.Vanilla, false)) continue;
                InteractionUtility.placeBlock(pos, rotate.getValue(), InteractionUtility.Interact.Vanilla, placeMode.getValue(), slot, false, true);
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
        }

        disable();
    }

    private void updateHighway(int slot) {
        if (buildMode.is(BuildMode.Baritone) && !baritoneStarted && hasBaritone) {
            startBaritone();
        }

        Direction facing = mc.player.getHorizontalFacing();
        Direction right = facing.rotateYClockwise();
        int half = highwayWidth.getValue() / 2;
        int groundY = BlockPos.ofFloored(mc.player.getPos()).getY() - 1;

        for (int i = 2; i <= 2; i++) {
            BlockPos seg = BlockPos.ofFloored(mc.player.getPos())
                    .withY(groundY)
                    .offset(facing, i);

            for (int w = -half; w <= half; w++) {
                BlockPos road = seg.offset(right, w);
                if (tryPlace(road, slot)) return;
            }

            BlockPos wl = seg.offset(right, -half).up();
            BlockPos wr = seg.offset(right, half).up();
            if (tryPlace(wl, slot)) return;
            if (tryPlace(wr, slot)) return;
        }
    }

    private void startBaritone() {
        try {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("tunnel");
            baritoneStarted = true;
        } catch (Throwable ignored) {
            hasBaritone = false;
        }
    }

    private void stopBaritone() {
        if (!baritoneStarted) return;
        try {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
        } catch (Throwable ignored) {
        }
        baritoneStarted = false;
    }

    private boolean tryPlace(BlockPos pos, int slot) {
        if (builtPositions.contains(pos)) return false;
        if (mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN) {
            builtPositions.add(pos);
            return false;
        }
        if (!InteractionUtility.canPlaceBlock(pos, InteractionUtility.Interact.Vanilla, false)) return false;

        InteractionUtility.placeBlock(pos, rotate.getValue(), InteractionUtility.Interact.Vanilla, placeMode.getValue(), slot, false, true);
        mc.player.swingHand(Hand.MAIN_HAND);
        builtPositions.add(pos);
        return true;
    }

    private enum Mode {
        Portal, Highway
    }

    private enum BuildMode {
        Simple, Baritone
    }
}

package thunder.hack.features.modules.player;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.player.InteractionUtility;
import thunder.hack.utility.player.InventoryUtility;
import thunder.hack.utility.player.SearchInvResult;
import thunder.hack.utility.render.BlockAnimationUtility;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

public class SourceFiller extends Module {
    private final Setting<Float> range = new Setting<>("Range", 5f, 1f, 7f);
    private final Setting<Integer> blocksPerTick = new Setting<>("Blocks/Tick", 4, 1, 12);
    private final Setting<Integer> placeDelay = new Setting<>("Delay", 1, 0, 10);
    private final Setting<InteractionUtility.Rotate> rotate = new Setting<>("Rotate", InteractionUtility.Rotate.Default);
    private final Setting<InteractionUtility.PlaceMode> placeMode = new Setting<>("Place Mode", InteractionUtility.PlaceMode.Packet);
    private final Setting<InteractionUtility.Interact> interact = new Setting<>("Interact", InteractionUtility.Interact.Vanilla);
    private final Setting<Boolean> swing = new Setting<>("Swing", true);

    private final Setting<SettingGroup> renderGroup = new Setting<>("Render", new SettingGroup(false, 0));
    private final Setting<BlockAnimationUtility.BlockAnimationMode> animationMode = new Setting<>("Animation", BlockAnimationUtility.BlockAnimationMode.Fade).addToGroup(renderGroup);
    private final Setting<BlockAnimationUtility.BlockRenderMode> renderMode = new Setting<>("Render Mode", BlockAnimationUtility.BlockRenderMode.All).addToGroup(renderGroup);
    private final Setting<ColorSetting> fillColor = new Setting<>("Fill Color", new ColorSetting(new Color(0, 100, 255, 50))).addToGroup(renderGroup);
    private final Setting<ColorSetting> lineColor = new Setting<>("Line Color", new ColorSetting(new Color(0, 100, 255, 150))).addToGroup(renderGroup);
    private final Setting<Integer> lineWidth = new Setting<>("Line Width", 2, 1, 5).addToGroup(renderGroup);

    private int delay;
    private BlockPos currentTarget;

    public SourceFiller() {
        super("SourceFiller", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        delay = 0;
        currentTarget = null;
    }

    @Override
    public void onDisable() {
        currentTarget = null;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        SearchInvResult spongeResult = findSponge();
        if (!spongeResult.found()) return;

        int placed = 0;
        while (placed < blocksPerTick.getValue()) {
            BlockPos target = findWaterSource();
            if (target == null) {
                currentTarget = null;
                break;
            }

            currentTarget = target;

            if (placeSponge(target, spongeResult.slot())) {
                placed++;
                delay = placeDelay.getValue();
            } else break;
        }
    }

    private SearchInvResult findSponge() {
        return InventoryUtility.findInHotBar(stack -> stack.getItem() == Items.SPONGE);
    }

    private BlockPos findWaterSource() {
        BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());
        int r = (int) Math.ceil(range.getValue());
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.player.squaredDistanceTo(pos.toCenterPos()) > range.getPow2Value()) continue;
                    BlockState state = mc.world.getBlockState(pos);
                    if (state.getBlock() == Blocks.WATER && state.getFluidState().isStill()) {
                        double dist = mc.player.squaredDistanceTo(pos.toCenterPos());
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearest = pos;
                        }
                    }
                }
            }
        }

        return nearest;
    }

    private boolean placeSponge(BlockPos pos, int slot) {
        if (!mc.world.getBlockState(pos).isReplaceable()) return false;

        boolean success = InteractionUtility.placeBlock(pos, rotate.getValue(), interact.getValue(), placeMode.getValue(), slot, false, true);

        if (success) {
            if (swing.getValue() && mc.player != null)
                mc.player.swingHand(Hand.MAIN_HAND);

            BlockAnimationUtility.renderBlock(pos,
                    lineColor.getValue().getColorObject(),
                    lineWidth.getValue(),
                    fillColor.getValue().getColorObject(),
                    animationMode.getValue(),
                    renderMode.getValue()
            );
        }

        return success;
    }

    @Override
    public void onRender3D(MatrixStack stack) {
        if (mc.player == null || mc.world == null || currentTarget == null) return;

        if (mc.world.getBlockState(currentTarget).getBlock() == Blocks.WATER) {
            Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(
                    new Box(currentTarget),
                    fillColor.getValue().getColorObject()
            ));
            Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(
                    new Box(currentTarget),
                    lineColor.getValue().getColorObject(),
                    lineWidth.getValue()
            ));
        }
    }
}

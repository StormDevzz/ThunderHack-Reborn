package thunder.hack.features.modules.player;

import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class MultiTask extends Module {
    public MultiTask() {
        super("MultiTask", Category.PLAYER);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.All);
    public final Setting<Float> range = new Setting<>("Range", 4.5f, 1f, 7f);

    public enum Mode {
        All, OnlyWhileUsing
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.OnlyWhileUsing && !mc.player.isUsingItem())
            return;

        if (mc.options.attackKey.isPressed()) {
            handleBlock();
            handleEntity();
        }
    }

    private void handleBlock() {
        if (mc.crosshairTarget instanceof BlockHitResult crossHair && crossHair.getBlockPos() != null
                && !mc.world.getBlockState(crossHair.getBlockPos()).isAir()) {
            mc.interactionManager.attackBlock(crossHair.getBlockPos(), crossHair.getSide());
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private void handleEntity() {
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr) || ehr.getEntity() == null) return;
        if (mc.player.squaredDistanceTo(ehr.getEntity()) > range.getValue() * range.getValue()) return;

        mc.interactionManager.attackEntity(mc.player, ehr.getEntity());
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}

package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import thunder.hack.events.impl.EventMove;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.MovementUtility;

public class WaterSpeed extends Module {
    public WaterSpeed() {
        super("WaterSpeed", Category.MOVEMENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.DolphinGrace);

    // Custom mode settings
    private final Setting<Float> customSpeed = new Setting<>("Speed", 0.3f, 0.01f, 2f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Float> customAcceleration = new Setting<>("Acceleration", 0.05f, 0.01f, 0.5f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> customDolphin = new Setting<>("DolphinGrace", false, v -> mode.getValue() == Mode.Custom);

    private float acceleration = 0f;

    public enum Mode {
        DolphinGrace, Intave, CancelResurface, FunTimeNew, Custom
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.DolphinGrace) {
            if (mc.player.isSwimming()) mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 2, 2));
            else mc.player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
        }

        if (mode.getValue() == Mode.Custom && customDolphin.getValue()) {
            if (mc.player.isSwimming()) mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 2, 0));
            else mc.player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
        }
    }

    @EventHandler
    public void onMove(EventMove e) {
        if (mode.getValue() == Mode.Intave) {
            if (mc.player.isSwimming()) {
                double[] dirSpeed = MovementUtility.forward(acceleration / (mc.player.input.movementSideways != 0 ? 2.2f : 2f));
                e.setX(e.getX() + dirSpeed[0]);
                e.setZ(e.getZ() + dirSpeed[1]);
                e.cancel();
                acceleration += 0.05f;
                acceleration = MathUtility.clamp(acceleration, 0f, 1f);
            } else acceleration = 0f;
            if (!MovementUtility.isMoving()) acceleration = 0f;
        }

        if (mode.getValue() == Mode.FunTimeNew) {
            if (mc.player.isSwimming()) {
                mc.player.input.movementSideways = 0;
                double[] dirSpeed = MovementUtility.forward(acceleration / 6.3447f);
                e.setX(e.getX() + dirSpeed[0]);
                e.setZ(e.getZ() + dirSpeed[1]);
                e.cancel();

                if (Math.abs(mc.player.getYaw() - mc.player.prevYaw) > 3) acceleration -= 0.1f;
                else acceleration += 0.015f;

                acceleration = MathUtility.clamp(acceleration, 0f, 1f);
            } else acceleration = 0f;
            if (!MovementUtility.isMoving() || mc.player.horizontalCollision || mc.player.verticalCollision) acceleration = 0f;
        }

        if (mode.getValue() == Mode.Custom) {
            if (mc.player.isSwimming() && mc.player.isTouchingWater()) {
                mc.player.input.movementSideways = 0;
                double[] dirSpeed = MovementUtility.forward(acceleration * customSpeed.getValue());
                e.setX(e.getX() + dirSpeed[0]);
                e.setZ(e.getZ() + dirSpeed[1]);
                e.cancel();

                acceleration += customAcceleration.getValue();
                acceleration = MathUtility.clamp(acceleration, 0f, 1f);
            } else acceleration = 0f;
            if (!MovementUtility.isMoving() || mc.player.horizontalCollision || mc.player.verticalCollision) acceleration = 0f;
        }
    }

    @Override
    public void onDisable() {
        if (mode.getValue() == Mode.DolphinGrace || (mode.getValue() == Mode.Custom && customDolphin.getValue()))
            mc.player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
    }
}
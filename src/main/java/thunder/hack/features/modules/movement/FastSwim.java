package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.EventMove;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.MovementUtility;

public class FastSwim extends Module {
    public FastSwim() {
        super("FastSwim", Category.MOVEMENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Custom);

    private final Setting<Float> waterSpeed = new Setting<>("WaterSpeed", 1.8f, 0.1f, 5f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Float> waterVertical = new Setting<>("WaterVertical", 1.2f, 0.1f, 5f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Float> lavaSpeed = new Setting<>("LavaSpeed", 1.5f, 0.1f, 5f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Float> lavaVertical = new Setting<>("LavaVertical", 1.2f, 0.1f, 5f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Logic> logic = new Setting<>("Logic", Logic.Set, v -> mode.getValue() == Mode.Custom);
    private final Setting<Float> acceleration = new Setting<>("Acceleration", 0.08f, 0.01f, 0.5f, v -> mode.getValue() == Mode.Strafe);
    private final Setting<Float> maxSpeed = new Setting<>("MaxSpeed", 2.5f, 0.1f, 5f, v -> mode.getValue() == Mode.Strafe);
    private final Setting<Boolean> noSink = new Setting<>("NoSink", true, v -> mode.getValue() == Mode.Strafe);

    private float accelerationVal = 0f;

    public enum Mode {
        Custom, Strafe
    }

    public enum Logic {
        Add, Set, Multiply
    }

    @Override
    public void onEnable() {
        accelerationVal = 0f;
    }

    @Override
    public void onDisable() {
        accelerationVal = 0f;
    }

    @EventHandler
    public void onMove(EventMove e) {
        if (!inFluid()) {
            accelerationVal = 0f;
            return;
        }

        if (mode.getValue() == Mode.Strafe) {
            if (MovementUtility.isMoving()) {
                accelerationVal += acceleration.getValue();
                accelerationVal = MathUtility.clamp(accelerationVal, 0f, maxSpeed.getValue());

                double[] dirSpeed = MovementUtility.forward(accelerationVal);
                e.setX(dirSpeed[0]);
                e.setZ(dirSpeed[1]);
                e.cancel();
            } else {
                accelerationVal = 0f;
            }

            if (noSink.getValue() && mc.player.getVelocity().y < 0 && !mc.player.input.playerInput.jump()) {
                e.setY(0);
                e.cancel();
            }
            return;
        }

        if (mode.getValue() == Mode.Custom) {
            if (!MovementUtility.isMoving()) return;

            float speed = isInLava() ? lavaSpeed.getValue() : waterSpeed.getValue();
            float vSpeed = isInLava() ? lavaVertical.getValue() : waterVertical.getValue();

            double[] dirSpeed = MovementUtility.forward(speed);

            switch (logic.getValue()) {
                case Add -> {
                    e.setX(e.getX() + dirSpeed[0]);
                    e.setZ(e.getZ() + dirSpeed[1]);
                }
                case Set -> {
                    e.setX(dirSpeed[0]);
                    e.setZ(dirSpeed[1]);
                }
                case Multiply -> {
                    e.setX(e.getX() * speed);
                    e.setZ(e.getZ() * speed);
                }
            }
            e.cancel();

            if (mc.player.input.playerInput.jump()) {
                e.setY(e.getY() + 0.04 * vSpeed);
            }
            if (mc.player.input.playerInput.sneak()) {
                e.setY(e.getY() - 0.04 * vSpeed);
            }
        }
    }

    private boolean isInLava() {
        return mc.player.isInLava();
    }

    private boolean inFluid() {
        return mc.player.isTouchingWater() || isInLava();
    }
}

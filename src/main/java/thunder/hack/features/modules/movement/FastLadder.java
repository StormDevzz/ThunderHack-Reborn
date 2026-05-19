package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.EventMove;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.player.MovementUtility;

public class FastLadder extends Module {
    public FastLadder() {
        super("FastLadder", Category.MOVEMENT);
    }

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Custom);

    // Custom mode settings
    private final Setting<Float> upSpeed = new Setting<>("UpSpeed", 0.3f, 0.1f, 2f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Float> downSpeed = new Setting<>("DownSpeed", 0.3f, 0.1f, 2f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Float> horizontalSpeed = new Setting<>("HorizontalSpeed", 0.3f, 0f, 2f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> onlyWhenMoving = new Setting<>("OnlyWhenMoving", true, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> noSlow = new Setting<>("NoSlow", true, v -> mode.getValue() == Mode.Custom);

    // Vanilla mode settings
    private final Setting<Float> vanillaSpeed = new Setting<>("Speed", 2f, 0.1f, 5f, v -> mode.getValue() == Mode.Vanilla);

    // Strafe mode settings
    private final Setting<Float> strafeAcceleration = new Setting<>("Acceleration", 0.04f, 0.01f, 0.2f, v -> mode.getValue() == Mode.Strafe);
    private final Setting<Float> strafeMaxSpeed = new Setting<>("MaxSpeed", 2f, 0.1f, 8f, v -> mode.getValue() == Mode.Strafe);

    private float strafeSpeed = 0f;

    public enum Mode {
        Custom, Vanilla, Strafe
    }

    @Override
    public void onEnable() {
        strafeSpeed = 0f;
    }

    @Override
    public void onDisable() {
        strafeSpeed = 0f;
    }

    @EventHandler
    public void onMove(EventMove e) {
        if (!mc.player.isClimbing() || !mc.player.horizontalCollision) {
            strafeSpeed = 0f;
            return;
        }

        if (mode.getValue() == Mode.Custom) {
            if (onlyWhenMoving.getValue() && !MovementUtility.isMoving() && !mc.player.input.playerInput.jump() && !mc.player.input.playerInput.sneak())
                return;

            Vec3d vel = mc.player.getVelocity();

            double moveY = 0;
            if (mc.player.input.playerInput.jump()) {
                moveY = upSpeed.getValue();
            } else if (mc.player.input.playerInput.sneak()) {
                moveY = -downSpeed.getValue();
            }

            if (noSlow.getValue() && moveY == 0) {
                moveY = vel.y > 0 ? vel.y : 0;
            }

            if (moveY != 0) {
                e.setY(moveY);
                e.cancel();
            }

            if (horizontalSpeed.getValue() > 0 && MovementUtility.isMoving()) {
                double[] dirSpeed = MovementUtility.forward(horizontalSpeed.getValue());
                e.setX(dirSpeed[0]);
                e.setZ(dirSpeed[1]);
                e.cancel();
            }
            return;
        }

        if (mode.getValue() == Mode.Vanilla) {
            if (!mc.player.input.playerInput.jump() && !mc.player.input.playerInput.sneak()) return;

            Vec3d vel = mc.player.getVelocity();
            if (mc.player.input.playerInput.jump()) {
                e.setY(vel.y * vanillaSpeed.getValue());
            } else if (mc.player.input.playerInput.sneak()) {
                e.setY(vel.y * vanillaSpeed.getValue());
            }
            e.cancel();
            return;
        }

        if (mode.getValue() == Mode.Strafe) {
            if (MovementUtility.isMoving()) {
                strafeSpeed += strafeAcceleration.getValue();
                strafeSpeed = MathUtility.clamp(strafeSpeed, 0f, strafeMaxSpeed.getValue());

                double[] dirSpeed = MovementUtility.forward(strafeSpeed);
                e.setX(dirSpeed[0]);
                e.setZ(dirSpeed[1]);
                e.cancel();
            } else {
                strafeSpeed = 0f;
            }

            if (mc.player.input.playerInput.jump()) {
                e.setY(strafeSpeed * 0.2);
                e.cancel();
            } else if (mc.player.input.playerInput.sneak()) {
                e.setY(-strafeSpeed * 0.2);
                e.cancel();
            }
        }
    }
}

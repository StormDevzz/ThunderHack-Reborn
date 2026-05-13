package thunder.hack.features.modules.movement;

import net.minecraft.client.util.math.MatrixStack;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class Parkour extends Module {
    public Parkour() {
        super("Parkour", Category.MOVEMENT);
    }

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.Default);

    // Default mode settings
    private final Setting<Float> jumpFactor = new Setting<>("JumpFactor", 0.01f, 0.001f, 0.3f, v -> mode.getValue() == Mode.Default);
    private final Setting<Integer> defaultDelay = new Setting<>("Delay", 150, 0, 500, v -> mode.getValue() == Mode.Default);
    private final Setting<Boolean> onlyOnEdge = new Setting<>("OnlyOnEdge", true, v -> mode.getValue() == Mode.Default);
    private final Setting<Boolean> autoSprint = new Setting<>("AutoSprint", false, v -> mode.getValue() == Mode.Default);
    private final Setting<Float> sprintSpeed = new Setting<>("SprintSpeed", 4.317f, 1.0f, 10.0f, v -> mode.getValue() == Mode.Default && autoSprint.getValue());
    private final Setting<Boolean> keepSprint = new Setting<>("KeepSprint", true, v -> mode.getValue() == Mode.Default && autoSprint.getValue());

    // Matrix mode settings
    private final Setting<Float> matrixJumpHeight = new Setting<>("MatrixJump", 0.42f, 0.1f, 1.0f, v -> mode.getValue() == Mode.Matrix);
    private final Setting<Integer> matrixDelay = new Setting<>("MatrixDelay", 200, 0, 1000, v -> mode.getValue() == Mode.Matrix);
    private final Setting<Boolean> matrixGroundSpoof = new Setting<>("MatrixGroundSpoof", true, v -> mode.getValue() == Mode.Matrix);
    private final Setting<Boolean> matrixCollisionCheck = new Setting<>("MatrixCollCheck", true, v -> mode.getValue() == Mode.Matrix);
    private final Setting<Boolean> matrixStrafe = new Setting<>("MatrixStrafe", true, v -> mode.getValue() == Mode.Matrix);
    private final Setting<Float> matrixStrafeSpeed = new Setting<>("MatrixStrafeSpd", 0.2f, 0.0f, 1.0f, v -> mode.getValue() == Mode.Matrix && matrixStrafe.getValue());

    private final thunder.hack.utility.Timer delay = new thunder.hack.utility.Timer();
    private boolean jumped;

    public void onRender3D(MatrixStack stack) {
        if (mode.getValue() == Mode.Default) {
            handleDefault();
        } else if (mode.getValue() == Mode.Matrix) {
            handleMatrix();
        }
    }

    private void handleDefault() {
        if (!MovementUtility.isMoving() || mc.player.isSneaking()) return;

        if (autoSprint.getValue()) {
            if (keepSprint.getValue() || MovementUtility.isMoving()) {
                mc.player.setSprinting(true);
            }
        }

        boolean nearEdge = !mc.world.getBlockCollisions(mc.player,
            mc.player.getBoundingBox()
                .expand(-jumpFactor.getValue(), 0, -jumpFactor.getValue())
                .offset(0, -0.99, 0)
        ).iterator().hasNext();

        if (mc.player.isOnGround()
                && !mc.options.jumpKey.isPressed()
                && (!onlyOnEdge.getValue() || nearEdge)
                && delay.every(defaultDelay.getValue())) {
            mc.player.jump();

            if (autoSprint.getValue() && jumped) {
                jumped = false;
            }
        }
    }

    private void handleMatrix() {
        if (!MovementUtility.isMoving() || mc.player.isSneaking()) return;

        if (matrixCollisionCheck.getValue() && mc.player.horizontalCollision) return;

        boolean nearEdge = !mc.world.getBlockCollisions(mc.player,
            mc.player.getBoundingBox()
                .expand(-0.01, 0, -0.01)
                .offset(0, -0.99, 0)
        ).iterator().hasNext();

        if (mc.player.isOnGround()
                && !mc.options.jumpKey.isPressed()
                && nearEdge
                && delay.every(matrixDelay.getValue())) {

            if (matrixGroundSpoof.getValue()) {
                mc.player.setOnGround(true);
                mc.player.prevY -= 2.0E-232;
            }

            mc.player.setVelocity(mc.player.getVelocity().x, matrixJumpHeight.getValue(), mc.player.getVelocity().z);

            if (matrixStrafe.getValue() && matrixStrafeSpeed.getValue() > 0) {
                double[] strafe = MovementUtility.forward(matrixStrafeSpeed.getValue());
                mc.player.setVelocity(strafe[0], mc.player.getVelocity().y, strafe[1]);
            }

            jumped = true;
        } else {
            jumped = false;
        }
    }

    @Override
    public void onEnable() {
        jumped = false;
    }

    @Override
    public void onDisable() {
        jumped = false;
    }

    public enum Mode {
        Default, Matrix
    }
}

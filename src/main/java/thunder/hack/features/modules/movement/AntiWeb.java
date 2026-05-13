package thunder.hack.features.modules.movement;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.util.math.Vec3d;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.EventCollision;
import thunder.hack.events.impl.EventMove;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.player.MovementUtility;

public class AntiWeb extends Module {
    public AntiWeb() {
        super("AntiWeb", Category.MOVEMENT);
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.Solid);
    public static final Setting<Boolean> grim = new Setting<>("Boolean", false, v -> mode.is(Mode.Ignore));
    public static final Setting<Float> timer = new Setting<>("Timer", 20f, 1f, 50f, v -> mode.getValue() == Mode.Timer);
    public Setting<Float> speed = new Setting<>("Speed", 0.3f, 0.0f, 10.0f, v -> mode.getValue() == Mode.Fly);

    // Custom mode settings
    private final Setting<Float> customHorizontal = new Setting<>("CustomH", 0.4f, 0.0f, 2.0f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Float> customVertical = new Setting<>("CustomV", 0.2f, 0.0f, 2.0f, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> customJump = new Setting<>("CustomJump", true, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> customSneak = new Setting<>("CustomSneak", true, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> customOnlyWeb = new Setting<>("CustomOnlyWeb", true, v -> mode.getValue() == Mode.Custom);
    private final Setting<Boolean> customStopMotion = new Setting<>("CustomStopMotion", true, v -> mode.getValue() == Mode.Custom);

    // Motion mode settings
    private final Setting<Float> motionHorizontal = new Setting<>("MotionH", 0.3f, 0.0f, 2.0f, v -> mode.getValue() == Mode.Motion);
    private final Setting<Float> motionVertical = new Setting<>("MotionV", 0.0f, -1.0f, 1.0f, v -> mode.getValue() == Mode.Motion);
    private final Setting<Boolean> motionResetY = new Setting<>("MotionResetY", true, v -> mode.getValue() == Mode.Motion);
    private final Setting<Boolean> motionAutoSprint = new Setting<>("MotionSprint", false, v -> mode.getValue() == Mode.Motion);

    private boolean timerEnabled = false;

    public enum Mode {
        Timer, Solid, Ignore, Fly, Custom, Motion
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent e) {
        boolean inWeb = Managers.PLAYER.isInWeb();

        if (mode.getValue() == Mode.Timer) {
            if (inWeb) {
                if (mc.player.isOnGround()) {
                    ThunderHack.TICK_TIMER = 1f;
                } else {
                    ThunderHack.TICK_TIMER = timer.getValue();
                    timerEnabled = true;
                }
            } else if (timerEnabled) {
                timerEnabled = false;
                ThunderHack.TICK_TIMER = 1f;
            }
        }

        if (mode.getValue() == Mode.Fly) {
            if (inWeb) {
                final double[] dir = MovementUtility.forward(speed.getValue());
                mc.player.setVelocity(dir[0], 0, dir[1]);
                if (mc.options.jumpKey.isPressed())
                    mc.player.setVelocity(mc.player.getVelocity().add(0, speed.getValue(), 0));
                if (mc.options.sneakKey.isPressed())
                    mc.player.setVelocity(mc.player.getVelocity().add(0, -speed.getValue(), 0));
            }
        }

        if (mode.getValue() == Mode.Custom) {
            if (!customOnlyWeb.getValue() || inWeb) {
                double motionX = 0;
                double motionZ = 0;

                if (MovementUtility.isMoving()) {
                    double[] dir = MovementUtility.forward(customHorizontal.getValue());
                    motionX = dir[0];
                    motionZ = dir[1];
                }

                double motionY = mc.player.getVelocity().y;
                if (customStopMotion.getValue()) motionY = 0;

                if (customJump.getValue() && mc.options.jumpKey.isPressed())
                    motionY += customVertical.getValue();
                if (customSneak.getValue() && mc.options.sneakKey.isPressed())
                    motionY -= customVertical.getValue();

                mc.player.setVelocity(motionX, motionY, motionZ);
            }
        }

        if (mode.getValue() == Mode.Motion) {
            if (inWeb) {
                Vec3d velocity = mc.player.getVelocity();

                double motionX = velocity.x;
                double motionZ = velocity.z;

                if (MovementUtility.isMoving()) {
                    double[] dir = MovementUtility.forward(motionHorizontal.getValue());
                    motionX = dir[0];
                    motionZ = dir[1];
                }

                double motionY = motionResetY.getValue() ? 0 : velocity.y;
                motionY += motionVertical.getValue();

                if (motionAutoSprint.getValue()) {
                    mc.player.setSprinting(true);
                }

                mc.player.setVelocity(motionX, motionY, motionZ);
            }
        }
    }

    @EventHandler
    public void onMove(EventMove e) {
        if (mode.getValue() == Mode.Custom && Managers.PLAYER.isInWeb()) {
            if (customStopMotion.getValue()) {
                double[] dir = MovementUtility.forward(customHorizontal.getValue());
                e.setX(dir[0]);
                e.setZ(dir[1]);
                e.cancel();
            }
        }
    }

    @EventHandler
    public void onCollide(EventCollision e) {
        if (e.getState().getBlock() instanceof CobwebBlock && mode.getValue() == Mode.Solid)
            e.setState(Blocks.DIRT.getDefaultState());
    }
}
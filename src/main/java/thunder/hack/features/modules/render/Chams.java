package thunder.hack.features.modules.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import thunder.hack.events.impl.EventHeldItemRenderer;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;

import java.awt.*;

public class Chams extends Module {
    public Chams() {
        super("Chams", Category.RENDER);
    }

    public final Setting<Boolean> handItems = new Setting<>("HandItems", false);
    private final Setting<ColorSetting> handItemsColor = new Setting<>("HandItemsColor", new ColorSetting(new Color(0x9317DE5D, true)), v -> handItems.getValue());

    public final Setting<Boolean> crystals = new Setting<>("Crystals", false);
    private final Setting<ColorSetting> crystalColor = new Setting<>("CrystalColor", new ColorSetting(new Color(0x932DD8E8, true)), v -> crystals.getValue());
    private final Setting<Boolean> staticCrystal = new Setting<>("StaticCrystal", true, v -> crystals.getValue());
    private final Setting<CMode> crystalMode = new Setting<>("CrystalMode", CMode.One, v -> crystals.getValue());

    public final Setting<Boolean> players = new Setting<>("Players", false);
    private final Setting<ColorSetting> playerColor = new Setting<>("PlayerColor", new ColorSetting(new Color(0x932DD8E8, true)), v -> players.getValue());
    private final Setting<ColorSetting> friendColor = new Setting<>("FriendColor", new ColorSetting(new Color(0x932DE830, true)), v -> players.getValue());
    private final Setting<Boolean> playerTexture = new Setting<>("PlayerTexture", true, v -> players.getValue());
    private final Setting<Boolean> simple = new Setting<>("Simple", false, v -> players.getValue());

    private final Setting<Boolean> alternativeBlending = new Setting<>("AlternativeBlending", true);

    private enum CMode {
        One, Two, Three
    }

    public void renderCrystal(Object state, Object matrixStack, int i, Object model) {
        // stubbed for 1.21.9
    }

    public void renderPlayer(Object state, Object matrixStack, int light, Object model, Object player) {
        // stubbed for 1.21.9
    }

    public void setupTransforms1(PlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h) {
        float j = abstractClientPlayerEntity.getLeaningPitch(h);
        float k = abstractClientPlayerEntity.getPitch(h);
        float l;
        float m;
        if (abstractClientPlayerEntity.isGliding()) {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            l = (float) (abstractClientPlayerEntity.age) + h;
            m = MathHelper.clamp(l * l / 100.0F, 0.0F, 1.0F);
            if (!abstractClientPlayerEntity.isUsingRiptide()) {
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m * (-90.0F - k)));
            }

            Vec3d vec3d = abstractClientPlayerEntity.getRotationVec(h);
            Vec3d vec3d2 = abstractClientPlayerEntity.getVelocity();
            double d = vec3d2.horizontalLengthSquared();
            double e = vec3d.horizontalLengthSquared();
            if (d > 0.0 && e > 0.0) {
                double n = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
                double o = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) (Math.signum(o) * Math.acos(n))));
            }
        } else if (j > 0.0F) {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
            l = abstractClientPlayerEntity.isTouchingWater() ? -90.0F - k : -90.0F;
            m = MathHelper.lerp(j, 0.0F, l);
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
            if (abstractClientPlayerEntity.isInSwimmingPose()) {
                matrixStack.translate(0.0F, -1.0F, 0.3F);
            }
        } else {
            setupTransforms(abstractClientPlayerEntity, matrixStack, f, g, h);
        }
    }

    private void setupTransforms(PlayerEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        if (!entity.isInPose(EntityPose.SLEEPING)) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - bodyYaw));
        }

        if (entity.deathTime > 0) {
            float f = ((float) entity.deathTime + tickDelta - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }

            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 90.0F));
        } else if (entity.isUsingRiptide()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F - entity.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(((float) entity.age + tickDelta) * -75.0F));
        } else if (entity.isInPose(EntityPose.SLEEPING)) {
            Direction direction = entity.getSleepingDirection();
            float g = direction != null ? getYaw(direction) : bodyYaw;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(g));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0F));
        }
    }

    private static float getYaw(Direction direction) {
        return switch (direction) {
            case NORTH -> 270.0f;
            case SOUTH -> 90.0f;
            case EAST -> 180.0f;
            default -> 0.0f;
        };
    }

    @EventHandler
    public void onRenderHands(EventHeldItemRenderer e) {
        if (handItems.getValue()) {
            // shader color setting disabled for 1.21.11
        }
    }
}
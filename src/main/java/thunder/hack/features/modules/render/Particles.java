package thunder.hack.features.modules.render;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.BooleanSettingGroup;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;

public class Particles extends Module {
    public Particles() {
        super("Particles", Category.RENDER);
    }

    private final Setting<BooleanSettingGroup> FireFlies = new Setting<>("FireFlies", new BooleanSettingGroup(true));
    private final Setting<Integer> ffcount = new Setting<>("FFCount", 30, 20, 200).addToGroup(FireFlies);
    private final Setting<Float> ffsize = new Setting<>("FFSize", 1f, 0.1f, 2.0f).addToGroup(FireFlies);
    private final Setting<Mode> mode = new Setting<>("Mode", Mode.SnowFlake);
    private final Setting<Integer> count = new Setting<>("Count", 100, 20, 800);
    private final Setting<Float> size = new Setting<>("Size", 1f, 0.1f, 6.0f);
    private final Setting<ColorMode> lmode = new Setting<>("ColorMode", ColorMode.Sync);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(3649978), v -> lmode.getValue() == ColorMode.Custom);
    private final Setting<Physics> physics = new Setting<>("Physics", Physics.Fly, v -> mode.getValue() != Mode.Off);

    private final ArrayList<ParticleBase> fireFlies = new ArrayList<>();
    private final ArrayList<ParticleBase> particles = new ArrayList<>();

    @Override
    public void onUpdate() {
        fireFlies.removeIf(ParticleBase::tick);
        particles.removeIf(ParticleBase::tick);

        for (int i = fireFlies.size(); i < ffcount.getValue(); i++) {
            if (FireFlies.getValue().isEnabled())
                fireFlies.add(new FireFly(
                        (float) (mc.player.getX() + MathUtility.random(-25f, 25f)),
                        (float) (mc.player.getY() + MathUtility.random(2f, 15f)),
                        (float) (mc.player.getZ() + MathUtility.random(-25f, 25f)),
                        MathUtility.random(-0.2f, 0.2f),
                        MathUtility.random(-0.1f, 0.1f),
                        MathUtility.random(-0.2f, 0.2f)));
        }

        for (int j = particles.size(); j < count.getValue(); j++) {
            boolean drop = physics.getValue() == Physics.Drop;
            if (mode.getValue() != Mode.Off)
                particles.add(new ParticleBase(
                        (float) (mc.player.getX() + MathUtility.random(-48f, 48f)),
                        (float) (mc.player.getY() + MathUtility.random(2, 48f)),
                        (float) (mc.player.getZ() + MathUtility.random(-48f, 48f)),
                        drop ? 0 : MathUtility.random(-0.4f, 0.4f),
                        drop ? MathUtility.random(-0.2f, -0.05f) : MathUtility.random(-0.1f, 0.1f),
                        drop ? 0 : MathUtility.random(-0.4f, 0.4f)));
        }
    }

    @Override
    public void onRender2D(DrawContext context) {
        if (mode.getValue() == Mode.Off && !FireFlies.getValue().isEnabled()) return;

        Identifier tex = switch (mode.getValue()) {
            case Stars -> TextureStorage.star;
            case Hearts -> TextureStorage.heart;
            case Dollars -> TextureStorage.dollar;
            case Bloom -> TextureStorage.firefly;
            default -> TextureStorage.snowflake;
        };

        float tickDelta = mc.getRenderTickCounter().getTickProgress(true);

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        for (ParticleBase p : particles) {
            Vec3d worldPos = new Vec3d(p.posX, p.posY, p.posZ);
            Vec3d ndc = mc.gameRenderer.project(worldPos);
            if (ndc == null || ndc.z > 1) continue;

            float sx = (float) ((ndc.x * 0.5 + 0.5) * sw);
            float sy = (float) ((-ndc.y * 0.5 + 0.5) * sh);

            float alpha = Math.max(0, Math.min(1, (float) p.age / p.maxAge));
            Color c = lmode.getValue() == ColorMode.Sync ? HudEditor.getColor(p.age * 10) : color.getValue().getColorObject();

            float s = size.getValue() * (1 + (1 - alpha) * 0.5f);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, tex,
                    (int) (sx - s / 2), (int) (sy - s / 2),
                    0f, 0f,
                    (int) s, (int) s, 16, 16,
                    ((int) (alpha * 255) << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue());
        }

        if (FireFlies.getValue().isEnabled()) {
            for (ParticleBase p : fireFlies) {
                Vec3d worldPos = new Vec3d(p.posX, p.posY, p.posZ);
                Vec3d ndc = mc.gameRenderer.project(worldPos);
                if (ndc == null || ndc.z > 1) continue;

                float sx = (float) ((ndc.x * 0.5 + 0.5) * sw);
                float sy = (float) ((-ndc.y * 0.5 + 0.5) * sh);

                float alpha = Math.max(0, Math.min(1, (float) p.age / p.maxAge));
                Color c = lmode.getValue() == ColorMode.Sync ? HudEditor.getColor(p.age * 10) : color.getValue().getColorObject();

                float s = ffsize.getValue() * 8 * (1 + (1 - alpha) * 0.5f);
                context.drawTexture(RenderPipelines.GUI_TEXTURED, TextureStorage.firefly,
                        (int) (sx - s / 2), (int) (sy - s / 2),
                        0f, 0f,
                        (int) s, (int) s, 16, 16,
                        ((int) (alpha * 255) << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue());
            }
        }
    }

    public void onRender3D(MatrixStack stack) {
    }

    public class FireFly extends ParticleBase {
        private final List<Trail> trails = new ArrayList<>();

        public FireFly(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
            super(posX, posY, posZ, motionX, motionY, motionZ);
        }

        @Override
        public boolean tick() {
            if (mc.player.squaredDistanceTo(posX, posY, posZ) > 100) age -= 4;
            else if (!mc.world.getBlockState(new BlockPos((int) posX, (int) posY, (int) posZ)).isAir()) age -= 8;
            else age--;

            if (age < 0) return true;

            trails.removeIf(Trail::update);

            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            Color trailColor = lmode.getValue() == ColorMode.Sync ? HudEditor.getColor(age * 10) : color.getValue().getColorObject();
            trails.add(new Trail(new Vec3d(prevposX, prevposY, prevposZ), new Vec3d(posX, posY, posZ), trailColor));

            motionX *= 0.99f;
            motionY *= 0.99f;
            motionZ *= 0.99f;

            return false;
        }
    }

    public class ParticleBase {
        protected float prevposX, prevposY, prevposZ, posX, posY, posZ, motionX, motionY, motionZ;
        protected int age, maxAge;

        public ParticleBase(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            age = (int) MathUtility.random(100, 300);
            maxAge = age;
        }

        public boolean tick() {
            if (mc.player.squaredDistanceTo(posX, posY, posZ) > 4096) age -= 8;
            else age--;

            if (age < 0) return true;

            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            motionX *= 0.9f;
            if (physics.getValue() == Physics.Fly)
                motionY *= 0.9f;
            motionZ *= 0.9f;

            motionY -= 0.001f;

            return false;
        }
    }

    public record Trail(Vec3d from, Vec3d to, Color color) {
        public boolean update() {
            return false;
        }
    }

    public enum ColorMode { Custom, Sync }
    public enum Mode { Off, SnowFlake, Stars, Hearts, Dollars, Bloom }
    public enum Physics { Drop, Fly }
}

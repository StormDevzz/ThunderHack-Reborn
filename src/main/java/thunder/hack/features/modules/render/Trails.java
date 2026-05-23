package thunder.hack.features.modules.render;
import net.minecraft.client.gl.RenderPipelines;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.interfaces.IEntity;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static thunder.hack.utility.render.Render2DEngine.applyOpacity;

public class Trails extends Module {
    public Trails() {
        super("Trails", Category.RENDER);
    }

    private final Setting<Boolean> xp = new Setting<>("Xp", false);
    private final Setting<Particles> pearls = new Setting<>("Pearls", Particles.Particles);
    private final Setting<Particles> arrows = new Setting<>("Arrows", Particles.Particles);
    private final Setting<Players> players = new Setting<>("Players", Players.Particles);
    private final Setting<Boolean> onlySelf = new Setting<>("OnlySelf", false, v -> players.getValue() != Players.None);
    private final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(0x8800FF00));
    private final Setting<Float> down = new Setting<>("Down", 0.5F, 0.0F, 2.0F);
    private final Setting<Float> width = new Setting<>("Height", 1.3F, 0.1F, 2.0F);
    private final Setting<Integer> speed = new Setting<>("Speed", 2, 1, 20, v -> players.is(Players.Particles) || arrows.is(Particles.Particles) || pearls.is(Particles.Particles));
    private final Setting<HitParticles.Mode> mode = new Setting<>("Mode", HitParticles.Mode.Stars);
    private final Setting<HitParticles.Physics> physics = new Setting<>("Physics", HitParticles.Physics.Fall, v -> players.is(Players.Particles) || arrows.is(Particles.Particles) || pearls.is(Particles.Particles));
    private final Setting<Integer> starsScale = new Setting<>("Scale", 3, 1, 10, v -> players.is(Players.Particles) || arrows.is(Particles.Particles) || pearls.is(Particles.Particles));
    private final Setting<Integer> amount = new Setting<>("Amount", 2, 1, 5, v -> players.is(Players.Particles) || arrows.is(Particles.Particles) || pearls.is(Particles.Particles));
    private final Setting<Integer> lifeTime = new Setting<>("LifeTime", 2, 1, 10, v -> players.is(Players.Particles) || arrows.is(Particles.Particles) || pearls.is(Particles.Particles));
    private final Setting<Mode> lmode = new Setting<>("ColorMode", Mode.Sync);
    private final Setting<ColorSetting> lcolor = new Setting<>("Color2", new ColorSetting(0x2250b4b4), v -> lmode.getValue() == Mode.Custom);

    private List<Particle> particles = new ArrayList<>();

    public void onRender3D(MatrixStack stack) {
    // stubbed for 1.21.9
}

    @Override
    public void onUpdate() {
        Color c = lmode.getValue() == Mode.Sync ? HudEditor.getColor(mc.player.age % 360) : lcolor.getValue().getColorObject();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player.getEntityPos().getZ() != player.lastZ || player.getEntityPos().getX() != player.lastX && (!onlySelf.getValue())) {
                ((IEntity) player).getTrails().add(new Trail(new Vec3d(player.lastX, player.lastY, player.lastZ), player.getEntityPos(), c));
                if (players.is(Players.Particles)) {
                    for (int i = 0; i < amount.getValue(); i++) {
                        particles.add(new Particle(player.getX(), MathUtility.random((float) (player.getY() + player.getHeight()), (float) player.getY()), player.getZ(), c));
                    }
                }
            }
            ((IEntity) player).getTrails().removeIf(Trail::update);

        }

        for (Entity en : Managers.ASYNC.getAsyncEntities()) {
            if (en instanceof ArrowEntity ae && (ae.lastY != ae.getY()) && arrows.is(Particles.Particles))
                for (int i = 0; i < 5; i++)
                    particles.add(new Particle(en.getX(), en.getY(), en.getZ(), HudEditor.getColor(mc.player.age)));

            if (en instanceof EnderPearlEntity && pearls.is(Particles.Particles))
                for (int i = 0; i < 5; i++)
                    particles.add(new Particle(en.getX(), en.getY(), en.getZ(), HudEditor.getColor(mc.player.age)));
        }

        if (Managers.PLAYER.currentPlayerSpeed != 0) {
            ((IEntity) mc.player).getTrails().add(new Trail(new Vec3d(mc.player.lastX, mc.player.lastY, mc.player.lastZ), mc.player.getEntityPos(), c));
            if (players.is(Players.Particles)) {
                for (int i = 0; i < amount.getValue(); i++) {
                    particles.add(new Particle(mc.player.getX(), MathUtility.random((float) (mc.player.getY() + mc.player.getHeight()), (float) mc.player.getY()), mc.player.getZ(), c));
                }
            }
        }
        particles.removeIf(particle -> System.currentTimeMillis() - particle.time > 1000f * lifeTime.getValue());
    }

    public static class Trail {
        private final Vec3d from;
        private final Vec3d to;
        private final Color color;
        private int ticks, prevTicks;

        public Trail(Vec3d from, Vec3d to, Color color) {
            this.from = from;
            this.to = to;
            this.ticks = 10;
            this.color = color;
        }

        public Vec3d interpolate(float pt) {
            double x = from.x + ((to.x - from.x) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getX();
            double y = from.y + ((to.y - from.y) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getY();
            double z = from.z + ((to.z - from.z) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
            return new Vec3d(x, y, z);
        }

        public double animation(float pt) {
            return (this.prevTicks + (this.ticks - this.prevTicks) * pt) / 10.;
        }

        public boolean update() {
            this.prevTicks = this.ticks;
            return this.ticks-- <= 0;
        }

        public Color color() {
            return color;
        }
    }

    private void calcTrajectory(Entity e) {
    // stubbed for 1.21.9
}

    public class Particle {
        double x;
        double y;
        double z;
        double motionX;
        double motionY;
        double motionZ;
        long time;
        Color color;

        public Particle(double x, double y, double z, Color color) {
            this.x = x;
            this.y = y;
            this.z = z;
            motionX = MathUtility.random(-(float) speed.getValue() / 200f, (float) speed.getValue() / 200f);
            motionY = MathUtility.random(-(float) speed.getValue() / 200f, (float) speed.getValue() / 200f);
            motionZ = MathUtility.random(-(float) speed.getValue() / 200f, (float) speed.getValue() / 200f);
            time = System.currentTimeMillis();
            this.color = color;
        }

        public void update() {
            double sp = starsScale.getValue() / 10f;
            x += motionX;
            y += motionY;
            z += motionZ;

            if (posBlock(x, y - starsScale.getValue() / 10f, z)) {
                motionY = -motionY / 1.1;
            } else {
                if (posBlock(x, y, z)
                        || posBlock(x - sp, y, z - sp)
                        || posBlock(x + sp, y, z + sp)
                        || posBlock(x + sp, y, z - sp)
                        || posBlock(x - sp, y, z + sp)
                        || posBlock(x + sp, y, z)
                        || posBlock(x - sp, y, z)
                        || posBlock(x, y, z - sp)
                        || posBlock(x, y, z + sp)
                ) {
                    motionX = -motionX;
                    motionZ = -motionZ;
                }
            }

            if (physics.getValue() == HitParticles.Physics.Fall) motionY -= 0.0005f;
            motionX /= 1.005;
            motionZ /= 1.005;
            motionY /= 1.005;
        }

        public void render(MatrixStack matrixStack, BufferBuilder bufferBuilder) {
    // stubbed for 1.21.9
}

        private boolean posBlock(double x, double y, double z) {
            Block b = mc.world.getBlockState(BlockPos.ofFloored(x, y, z)).getBlock();
            return b != Blocks.AIR && b != Blocks.WATER && b != Blocks.LAVA;
        }
    }

    private enum Mode {
        Custom, Sync
    }

    private enum Players {
        Trail, Particles, Cute, Tail, None
    }

    private enum Particles {
        Trail, Particles, None
    }
}

package thunder.hack.features.hud.impl;
import net.minecraft.client.gl.RenderPipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2fStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL40C;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.gui.hud.HudEditorGui;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.features.modules.combat.AutoAnchor;
import thunder.hack.features.modules.combat.AutoCrystal;
import thunder.hack.features.modules.misc.NameProtect;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;
import thunder.hack.utility.render.animation.EaseOutBack;
import thunder.hack.utility.render.animation.EaseOutCirc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TargetHud extends HudElement {
    private final Setting<Integer> blurRadius = new Setting<>("BallonBlur", 10, 1, 10);
    private final Setting<Integer> animX = new Setting<>("AnimationX", 0, -2000, 2000);
    private final Setting<Integer> animY = new Setting<>("AnimationY", 0, -2000, 2000);
    private final Setting<HPmodeEn> hpMode = new Setting<>("HP Mode", HPmodeEn.HP);
    private final Setting<ImageModeEn> imageMode = new Setting<>("Image", ImageModeEn.Anime);
    private final Setting<ModeEn> Mode = new Setting<>("Mode", ModeEn.ThunderHack);
    private final Setting<ColorSetting> color = new Setting<>("Color1", new ColorSetting(-16492289), v -> Mode.getValue() == ModeEn.CelkaPasta);
    private final Setting<ColorSetting> color2 = new Setting<>("Color2", new ColorSetting(-16492289), v -> Mode.getValue() == ModeEn.CelkaPasta);
    private final Setting<Boolean> funTimeHP = new Setting<>("FunTimeHP", false);
    private final Setting<Boolean> mini = new Setting<>("Mini", false, v -> Mode.getValue() == ModeEn.NurikZapen);
    private final Setting<Boolean> absorp = new Setting<>("Absorption", true);

    private static Identifier custom;

    public EaseOutBack animation = new EaseOutBack();
    public static EaseOutCirc healthAnimation = new EaseOutCirc();
    public static EaseOutCirc headAnimation = new EaseOutCirc();

    private final ArrayList<Particles> particles = new ArrayList<>();

    private boolean sentParticles, direction = false;
    private LivingEntity target;

    float ticks;

    private final Timer timer = new Timer();

    public TargetHud() {
        super("TargetHud", 150, 50);
    }

    @Override
    public void onEnable() {
        try {
            custom = ThunderUtility.getCustomImg("thud");
        } catch (Exception e) {
            sendMessage(".minecraft -> ThunderHackRecode -> misc -> images -> thud.png");
        }
    }

    public String getDurationString(StatusEffectInstance pe) {
        if (pe.isInfinite()) {
            return "*:*";
        } else return pe.getDuration() / 1200 + ":" + (pe.getDuration() % 1200) / 20;
    }

    @Override
    public void onUpdate() {
        animation.update(direction);
        healthAnimation.update();
        headAnimation.update();
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        getTarget();
        if (target == null) return;

        float health = Math.min(target.getMaxHealth(), getHealth());

        if (animation.getAnimationd() > 0) {
            float animationFactor = (float) MathUtility.clamp(animation.getAnimationd(), 0, 1f);
            switch (Mode.getValue()) {
                case ThunderHack -> renderThunderHack(context, health, animationFactor);
                case CelkaPasta -> renderCelkaPasta(context, health);
                case NurikZapen -> {
                    if (mini.getValue())
                        renderMiniNurik(context, health, animationFactor);
                    else
                        renderNurik(context, health, animationFactor);

                }
            }
        }
    }

    private void getTarget() {
        if (AutoCrystal.target != null) {
            target = AutoCrystal.target;
            direction = true;
            if (AutoCrystal.target.isDead()) {
                AutoCrystal.target = null;
                target = null;
            }
        } else if (Aura.target != null) {
            if (Aura.target instanceof LivingEntity) {
                target = (LivingEntity) Aura.target;
                direction = true;
            } else {
                target = null;
                direction = false;
            }
        } else if (AutoAnchor.target != null) {
            target = AutoAnchor.target;
            direction = true;
            if (AutoAnchor.target.isDead()) {
                AutoAnchor.target = null;
                target = null;
            }
        } else if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof HudEditorGui) {
            target = mc.player;
            direction = true;
        } else {
            direction = false;
            if (animation.getAnimationd() < 0.02)
                target = null;
        }
    }

    private void renderCelkaPasta(DrawContext context, float health) {
        // stubbed for 1.21.9
    }

    private void renderNurik(DrawContext context, float health, float animationFactor) {
        // stubbed for 1.21.9
    }

    private void renderMiniNurik(DrawContext context, float health, float animationFactor) {
        // stubbed for 1.21.9
    }

    private void renderThunderHack(DrawContext context, float health, float animationFactor) {
        // stubbed for 1.21.9
    }

    private void renderThunderHackPlus(DrawContext context, float health, float animationFactor) {
        // stubbed for 1.21.9
    }

    public static Color TwoColoreffect(Color cl1, Color cl2, double speed) {
        double thing = speed / 4.0 % 1.0;
        float val = MathHelper.clamp((float)(Math.sin(Math.PI * 6 * thing) / 2.0 + 0.5), 0.0f, 1.0f);
        return new Color(
            lerpColorComponent(cl1.getRed(), cl2.getRed(), val),
            lerpColorComponent(cl1.getGreen(), cl2.getGreen(), val),
            lerpColorComponent(cl1.getBlue(), cl2.getBlue(), val)
        );
    }

    private static int lerpColorComponent(int a, int b, float f) {
        return (int)(a + f * (b - a));
    }

    private void celestialArmor(DrawContext context, PlayerEntity target, float posX, float posY) {
        // stubbed for 1.21.9
    }

    private void celestialHands(DrawContext context, PlayerEntity target, float posX, float posY) {
        // stubbed for 1.21.9
    }

    private void drawPotionEffect(MatrixStack ms, PlayerEntity entity) {
        // stubbed for 1.21.9
    }

    public float getHealth() {
        return (absorp.getValue()) ? target.getHealth() + target.getAbsorptionAmount() : target.getHealth();
    }

    public static void sizeAnimation(Matrix3x2fStack matrixStack, double width, double height, double animation) {
        // stubbed for 1.21.9
    }

    public static String getPotionName(StatusEffect p) {
        if (p == StatusEffects.REGENERATION.value()) return "Reg";
        else if (p == StatusEffects.STRENGTH.value()) return "Str";
        else if (p == StatusEffects.SPEED.value()) return "Spd";
        else if (p == StatusEffects.HASTE.value()) return "H";
        else if (p == StatusEffects.WEAKNESS.value()) return "W";
        else if (p == StatusEffects.RESISTANCE.value()) return "Res";
        return "pon";
    }

    private Identifier getEntityTexture(LivingEntity entity) {
        return Identifier.of("minecraft:textures/mob/no_bg.png");
    }

    public enum HPmodeEn {
        HP, Percentage
    }

    public enum ImageModeEn {
        None, Anime, Custom
    }

    public enum ModeEn {
        ThunderHack, NurikZapen, CelkaPasta
    }
}
package thunder.hack.features.modules.render;
import net.minecraft.client.gl.RenderPipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.EffectParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import thunder.hack.core.Managers;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.injection.accesors.IAreaEffectCloudEntity;
import thunder.hack.injection.accesors.IBeaconBlockEntity;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.setting.impl.SettingGroup;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.awt.*;

import static thunder.hack.utility.render.animation.AnimationUtility.fast;

public class ESP extends Module {
    public ESP() {
        super("ESP", Category.RENDER);
    }

    private final Setting<Boolean> lingeringPotions = new Setting<>("LingeringPotions", false);
    private final Setting<Boolean> tntFuse = new Setting<>("TNTFuse", false);
    private final Setting<Float> tntrange = new Setting<>("TNTRange", 8.0f, 0f, 8f);
    private final Setting<ColorSetting> tntFuseText = new Setting<>("TNTFuseText", new ColorSetting(new Color(-1)), v -> tntFuse.getValue());
    private final Setting<Boolean> tntRadius = new Setting<>("TNTRadius", false);
    private final Setting<ColorSetting> tntRadiusColor = new Setting<>("TNTSphereColor", new ColorSetting(new Color(-1)), v -> tntRadius.getValue());
    private final Setting<Boolean> beaconRadius = new Setting<>("BeaconRadius", false);
    private final Setting<Boolean> keepY = new Setting<>("KeepY", false, v -> beaconRadius.getValue());
    private final Setting<ColorSetting> sphereColor = new Setting<>("SphereColor", new ColorSetting(new Color(-1)), v -> beaconRadius.getValue());
    private final Setting<ColorSetting> beakonColor = new Setting<>("BeakonColor", new ColorSetting(new Color(-1)), v -> beaconRadius.getValue());
    private final Setting<Boolean> burrow = new Setting<>("Burrow", false);
    private final Setting<ColorSetting> burrowTextColor = new Setting<>("BurrowTextColor", new ColorSetting(new Color(-1)), v -> burrow.getValue());
    private final Setting<ColorSetting> burrowColor = new Setting<>("BurrowColor", new ColorSetting(new Color(-1)), v -> burrow.getValue());
    private final Setting<Boolean> pearls = new Setting<>("Pearls", false);
    private final Setting<Boolean> dizorentRadius = new Setting<>("DizorentRadius", true);
    private final Setting<ColorSetting> dizorentColor = new Setting<>("DizorentColor", new ColorSetting(new Color(0xB300F1CC, true)), v -> dizorentRadius.getValue());

    private final Setting<SettingGroup> boxEsp = new Setting<>("Box", new SettingGroup(false, 0));
    private final Setting<Boolean> players = new Setting<>("Players", true).addToGroup(boxEsp);
    private final Setting<Boolean> friends = new Setting<>("Friends", true).addToGroup(boxEsp);
    private final Setting<Boolean> crystals = new Setting<>("Crystals", true).addToGroup(boxEsp);
    private final Setting<Boolean> creatures = new Setting<>("Creatures", false).addToGroup(boxEsp);
    private final Setting<Boolean> monsters = new Setting<>("Monsters", false).addToGroup(boxEsp);
    private final Setting<Boolean> ambients = new Setting<>("Ambients", false).addToGroup(boxEsp);
    private final Setting<Boolean> others = new Setting<>("Others", false).addToGroup(boxEsp);
    private final Setting<Boolean> outline = new Setting<>("Outline", true).addToGroup(boxEsp);
    private final Setting<Colors> colorMode = new Setting<>("ColorMode", Colors.SyncColor).addToGroup(boxEsp);
    private final Setting<Boolean> renderHealth = new Setting<>("renderHealth", true).addToGroup(boxEsp);

    private final Setting<SettingGroup> boxColors = new Setting<>("BoxColors", new SettingGroup(false, 0));
    private final Setting<ColorSetting> playersC = new Setting<>("PlayersC", new ColorSetting(new Color(0xFF9200))).addToGroup(boxColors);
    private final Setting<ColorSetting> friendsC = new Setting<>("FriendsC", new ColorSetting(new Color(0x30FF00))).addToGroup(boxColors);
    private final Setting<ColorSetting> crystalsC = new Setting<>("CrystalsC", new ColorSetting(new Color(0x00BBFF))).addToGroup(boxColors);
    private final Setting<ColorSetting> creaturesC = new Setting<>("CreaturesC", new ColorSetting(new Color(0xA0A4A6))).addToGroup(boxColors);
    private final Setting<ColorSetting> monstersC = new Setting<>("MonstersC", new ColorSetting(new Color(0xFF0000))).addToGroup(boxColors);
    private final Setting<ColorSetting> ambientsC = new Setting<>("AmbientsC", new ColorSetting(new Color(0x7B00FF))).addToGroup(boxColors);
    private final Setting<ColorSetting> othersC = new Setting<>("OthersC", new ColorSetting(new Color(0xFF0062))).addToGroup(boxColors);
    public final Setting<ColorSetting> healthB = new Setting<>("healthB", new ColorSetting(new Color(0xff1100))).addToGroup(boxColors);
    public final Setting<ColorSetting> healthU = new Setting<>("healthU", new ColorSetting(new Color(0x2fff00))).addToGroup(boxColors);

    private float dizorentAnimation = 0f;

    public void onRender3D(MatrixStack stack) {
        // stubbed for 1.21.9
    }

    public void onRender2D(DrawContext context) {
        // stubbed for 1.21.9
    }

    public static float getRotations(Vec2f vec) {
        if (mc.player == null) return 0;
        double x = vec.x - mc.player.getEntityPos().x;
        double z = vec.y - mc.player.getEntityPos().z;
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }

    public enum Colors {
        SyncColor, Custom
    }
}

package thunder.hack.features.modules.render;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL11;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.player.FriendManager;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.impl.PotionHud;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.misc.NameProtect;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.*;

public class NameTags extends Module {
    private final Map<RegistryKey<Enchantment>, String> encMap = new HashMap<>();

    public NameTags() {
        super("NameTags", Category.RENDER);
        encMap.put(Enchantments.BLAST_PROTECTION, "B");
        encMap.put(Enchantments.PROTECTION, "P");
        encMap.put(Enchantments.SHARPNESS, "S");
        encMap.put(Enchantments.EFFICIENCY, "E");
        encMap.put(Enchantments.UNBREAKING, "U");
        encMap.put(Enchantments.POWER, "PO");
        encMap.put(Enchantments.THORNS, "T");
    }

    private final Setting<Boolean> self = new Setting<>("Self", false);
    private final Setting<Float> scale = new Setting<>("Scale", 1f, 0.1f, 10f);
    private final Setting<Boolean> resize = new Setting<>("Resize", false);
    private final Setting<Float> height = new Setting<>("Height", 2f, 0.1f, 10f);
    private final Setting<Boolean> gamemode = new Setting<>("Gamemode", false);
    private final Setting<Boolean> spawners = new Setting<>("SpawnerNameTag", false);
    private final Setting<Boolean> entityOwner = new Setting<>("EntityOwner", false);
    private final Setting<Boolean> ping = new Setting<>("Ping", false);
    private final Setting<Boolean> hp = new Setting<>("HP", true);
    private final Setting<Boolean> distance = new Setting<>("Distance", true);
    private final Setting<Boolean> pops = new Setting<>("TotemPops", true);
    private final Setting<OutlineColor> outline = new Setting<>("OutlineType", OutlineColor.New);
    private final Setting<OutlineColor> friendOutline = new Setting<>("FriendOutline", OutlineColor.None);
    private final Setting<ColorSetting> outlineColor = new Setting<>("OutlineColor", new ColorSetting(0x80000000));
    private final Setting<ColorSetting> friendOutlineColor = new Setting<>("FriendOutlineColor", new ColorSetting(0x80000000));
    private final Setting<Boolean> enchantss = new Setting<>("Enchants", true);
    private final Setting<Boolean> onlyHands = new Setting<>("OnlyHands", false, v -> enchantss.getValue());
    private final Setting<Boolean> funtimeHp = new Setting<>("FunTimeHp", false);
    private final Setting<Boolean> ignoreBots = new Setting<>("IgnoreBots", false);
    private final Setting<Boolean> potions = new Setting<>("Potions", true);
    private final Setting<Boolean> shulkers = new Setting<>("Shulkers", true);
    private final Setting<ColorSetting> fillColorA = new Setting<>("Fill", new ColorSetting(0x80000000));
    private final Setting<ColorSetting> fillColorF = new Setting<>("FriendFill", new ColorSetting(0x80000000));
    private final Setting<Font> font = new Setting<>("FontMode", Font.Fancy);
    private final Setting<Armor> armorMode = new Setting<>("ArmorMode", Armor.Full);
    private final Setting<Health> health = new Setting<>("Health", Health.Number);


    public void onRender2D(DrawContext context) {
        // stubbed for 1.21.9
    }

    private void drawSpawnerNameTag(DrawContext context) {
        // stubbed for 1.21.9
    }

    public void drawEntityOwner(DrawContext context) {
    // stubbed for 1.21.9
}

    public static int getEntityPing(PlayerEntity entity) {
        if (mc.getNetworkHandler() == null) return 0;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }

    public static GameMode getEntityGamemode(PlayerEntity entity) {
        if (entity == null) return null;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        return playerListEntry == null ? null : playerListEntry.getGameMode();
    }

    private String translateGamemode(GameMode gamemode) {
        // stubbed for 1.21.9
        return null;
    }

    public float getHealth(PlayerEntity ent) {
        // stubbed for 1.21.9
        return ent.getHealth() + ent.getAbsorptionAmount();
    }

    private void renderHealthBar(DrawContext context, PlayerEntity player, float maxHealth, int lastHealth, int absorption) {
        // stubbed for 1.21.9
    }

    private void drawHeart(DrawContext context, HeartType type, int x, boolean half, PlayerEntity player) {
        // stubbed for 1.21.9
    }

    private enum HeartType {
        CONTAINER(Identifier.of("hud/heart/container"), Identifier.of("hud/heart/container")), NORMAL(Identifier.of("hud/heart/full"), Identifier.of("hud/heart/half")), ABSORBING(Identifier.of("hud/heart/absorbing_full"), Identifier.of("hud/heart/absorbing_half"));

        private final Identifier fullTexture;
        private final Identifier halfTexture;

        HeartType(Identifier fullTexture, Identifier halfTexture) {
            this.fullTexture = fullTexture;
            this.halfTexture = halfTexture;
        }

        public Identifier getTexture(boolean half) {
            return half ? halfTexture : fullTexture;
        }
    }

    public @NotNull String getHealthColor(float health) {
        if (health <= 15 && health > 7) return Formatting.YELLOW + "";
        if (health > 15) return Formatting.GREEN + "";
        return Formatting.RED + "";
    }

    public @NotNull String getPingColor(int ping) {
        if (ping <= 60) return Formatting.GREEN + "";
        if (ping > 60 && ping < 120) return Formatting.YELLOW + "";
        return Formatting.RED + "";
    }

    private @NotNull Color getHealthColor2(float health) {
        if (health <= 15 && health > 7) return Color.YELLOW;
        if (health > 15) return Color.GREEN;
        return Color.RED;
    }

    public static float round2(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return 1f;
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    private void renderStatusEffectOverlay(DrawContext context, float x, float y, PlayerEntity player) {
        // stubbed for 1.21.9
    }

    public boolean renderShulkerToolTip(DrawContext context, int offsetX, int offsetY, ItemStack stack) {
        try {
            ContainerComponent compoundTag = stack.get(DataComponentTypes.CONTAINER);
            if (compoundTag == null) return false;

            float[] colors = new float[]{1F, 1F, 1F};
            Item focusedItem = stack.getItem();
            if (focusedItem instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
                try {
                    Color c = new Color(Objects.requireNonNull(((ShulkerBoxBlock) bi.getBlock()).getColor()).getEntityColor());
                    colors = new float[]{c.getRed() / 255f, c.getGreen() / 255f, c.getRed() / 255f, c.getAlpha() / 255f};
                } catch (NullPointerException npe) {
                    colors = new float[]{1F, 1F, 1F};
                }
            } else {
                return false;
            }
            draw(context, compoundTag.stream().toList(), offsetX, offsetY, colors);
        } catch (Exception ignore) {
            return false;
        }
        return true;
    }

    private void draw(DrawContext context, List<ItemStack> itemStacks, int offsetX, int offsetY, float[] colors) {
        // stubbed for 1.21.9
    }

    private void drawBackground(DrawContext context, int x, int y, float[] colors) {
        // stubbed for 1.21.9
    }

    public enum Font {
        Fancy, Fast
    }

    public enum Armor {
        None, Full, Durability
    }

    public enum Health {
        Number, Hearts, Dots
    }

    private enum OutlineColor {
        Sync, Custom, None, New
    }
}
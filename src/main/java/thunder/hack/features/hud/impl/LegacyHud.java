package thunder.hack.features.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.StringUtils;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.gui.font.FontRenderer;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.math.FrameRateCounter;
import thunder.hack.utility.math.MathUtility;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static thunder.hack.features.hud.impl.PotionHud.getDuration;

public class LegacyHud extends Module {
    public LegacyHud() {
        super("LegacyHud", Category.HUD);
    }

    private static final ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);

    private final Setting<Font> customFont = new Setting<>("Font", Font.Minecraft);
    private final Setting<ColorSetting> colorSetting = new Setting<>("Color", new ColorSetting(new Color(0x0077FF)));
    private final Setting<Boolean> renderingUp = new Setting<>("RenderingUp", false);
    private final Setting<Boolean> waterMark = new Setting<>("Watermark", false);
    private final Setting<Boolean> arrayList = new Setting<>("ActiveModules", false);
    private final Setting<Boolean> coords = new Setting<>("Coords", false);
    private final Setting<Boolean> direction = new Setting<>("Direction", false);
    private final Setting<Boolean> armor = new Setting<>("Armor", false);
    private final Setting<Boolean> totems = new Setting<>("Totems", false);
    private final Setting<Boolean> greeter = new Setting<>("Welcomer", false);
    private final Setting<Boolean> speed = new Setting<>("Speed", false);
    private final Setting<Boolean> bps = new Setting<>("BPS", false, v -> speed.getValue());
    public final Setting<Boolean> potions = new Setting<>("Potions", false);
    private final Setting<Boolean> ping = new Setting<>("Ping", false);
    private final Setting<Boolean> tps = new Setting<>("TPS", false);
    private final Setting<Boolean> extraTps = new Setting<>("ExtraTPS", true, v -> tps.getValue());
    private final Setting<Boolean> offhandDurability = new Setting<>("OffhandDurability", false);
    private final Setting<Boolean> mainhandDurability = new Setting<>("MainhandDurability", false);
    private final Setting<Boolean> fps = new Setting<>("FPS", false);
    private final Setting<Boolean> chests = new Setting<>("Chests", false);
    private final Setting<Boolean> worldTime = new Setting<>("WorldTime", false);
    private final Setting<Boolean> biome = new Setting<>("Biome", false);
    public Setting<Boolean> time = new Setting<>("Time", false);

    public Setting<Integer> waterMarkY = new Setting<>("WatermarkPosY", 2, 0, 20, v -> waterMark.getValue());
    private int color;

    private enum Font {
        Minecraft, Comfortaa, Monsterrat, SF
    }

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}

    private void drawText(DrawContext context, String str, int x, int y, int color) {
        if (!customFont.getValue().equals(Font.Minecraft)) {
            FontRenderer adapter;
            switch (customFont.getValue()) {
                case Monsterrat -> adapter = FontRenderers.monsterrat;
                case SF -> adapter = FontRenderers.sf_medium;
                default -> adapter = FontRenderers.modules;
            }
            adapter.drawString(context.getMatrices(), str.replace(Formatting.WHITE + "", ""), x + 0.5, y + 0.5, Color.BLACK.getRGB());
            adapter.drawString(context.getMatrices(), str, x, y, color);
            return;
        }
        context.drawText(mc.textRenderer, str, x, y, color, true);
    }

    private void drawText(DrawContext context, String str, int x, int y) {
        if (!customFont.getValue().equals(Font.Minecraft)) {
            FontRenderer adapter;
            switch (customFont.getValue()) {
                case Monsterrat -> adapter = FontRenderers.monsterrat;
                case SF -> adapter = FontRenderers.sf_medium;
                default -> adapter = FontRenderers.modules;
            }
            adapter.drawString(context.getMatrices(), str.replace(Formatting.WHITE + "", ""), x + 0.5, y + 0.5, Color.BLACK.getRGB());
            adapter.drawString(context.getMatrices(), str, x, y, color);
            return;
        }
        context.drawText(mc.textRenderer, str, x, y, color, true);
    }

    private int getStringWidth(String str) {
        switch (customFont.getValue()) {
            case Monsterrat -> {
                return (int) FontRenderers.monsterrat.getStringWidth(str);
            }
            case SF -> {
                return (int) FontRenderers.sf_medium.getStringWidth(str);
            }
            case Minecraft -> {
                return mc.textRenderer.getWidth(str);
            }
            default -> {
                return (int) FontRenderers.modules.getStringWidth(str);
            }
        }
    }

    public void renderGreeter(DrawContext context) {
        // stubbed for 1.21.9
    }

    public void renderTotemHUD(DrawContext context) {
        // stubbed for 1.21.9
    }

    public void renderArmorHUD(boolean percent, DrawContext context) {
        // stubbed for 1.21.9
    }

    public static String getTimeOfDay() {
        int timeOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (timeOfDay < 12) return "Morning ";
        if (timeOfDay < 16) return "Afternoon ";
        if (timeOfDay < 21) return "Evening ";
        return "Night ";
    }

    private static String biome() {
        if (mc.player == null || mc.world == null) return null;
        Identifier id = mc.world.getRegistryManager().getOrThrow(RegistryKeys.BIOME).getId(mc.world.getBiome(mc.player.getBlockPos()).value());
        if (id == null) return ("Unknown");

        return (Arrays.stream(id.getPath().split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" ")));
    }
}

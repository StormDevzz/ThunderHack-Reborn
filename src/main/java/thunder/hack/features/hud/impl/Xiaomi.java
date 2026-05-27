package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import thunder.hack.features.hud.HudElement;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Xiaomi extends HudElement {
    private static final Identifier XIAOMI = Identifier.of("thunderhack", "textures/hud/elements/xiaomi.png");
    private static final Identifier XIAOMI1 = Identifier.of("thunderhack", "textures/hud/elements/xiaomi1.png");
    private static final Identifier XIAOMI2 = Identifier.of("thunderhack", "textures/hud/elements/xiaomi2.png");

    private final Setting<Boolean> img1Enabled = new Setting<>("Image1", true);
    private final Setting<String> img1Label = new Setting<>("Label1", "Xiaomi");
    private final Setting<Boolean> img2Enabled = new Setting<>("Image2", true);
    private final Setting<String> img2Label = new Setting<>("Label2", "Xiaomi 1");
    private final Setting<Boolean> img3Enabled = new Setting<>("Image3", true);
    private final Setting<String> img3Label = new Setting<>("Label3", "Xiaomi 2");
    private final Setting<Integer> imageSize = new Setting<>("ImageSize", 64, 16, 128);
    private final Setting<Integer> gap = new Setting<>("Gap", 8, 0, 32);

    private List<ImageEntry> cachedImages = List.of();

    private record ImageEntry(Identifier id, String label) {}

    public Xiaomi() {
        super("Xiaomi", 220, 100);
    }

    @Override
    public void onUpdate() {
        List<ImageEntry> newList = new ArrayList<>(3);
        if (img1Enabled.getValue()) newList.add(new ImageEntry(XIAOMI, img1Label.getValue()));
        if (img2Enabled.getValue()) newList.add(new ImageEntry(XIAOMI1, img2Label.getValue()));
        if (img3Enabled.getValue()) newList.add(new ImageEntry(XIAOMI2, img3Label.getValue()));
        cachedImages = newList;
    }

    @Override
    public void onRender2D(DrawContext context) {
        // stubbed for 1.21.9
    }
}

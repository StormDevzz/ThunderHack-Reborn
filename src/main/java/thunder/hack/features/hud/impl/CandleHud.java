package thunder.hack.features.hud.impl;

import net.minecraft.client.gui.DrawContext;
import thunder.hack.features.hud.HudElement;
import thunder.hack.setting.Setting;

import java.awt.*;

public class CandleHud extends HudElement {
    public CandleHud() {
        super("Candle", 10, 100);
    }

    private Setting<Integer> scale = new Setting<>("Scale", 25, 15, 100);
    private Setting<For> mode = new Setting<>("For", For.Win);

    private float xAnim, yAnim, prevPitch;

    public void onRender2D(DrawContext context) {
    }

    @SuppressWarnings("unused")
    private void drawFire(Object matrices, float x, float y, float width, float height, Color color) {
    }

    private enum For {
        Luck, Win, LowPing, AntiKick, SuperZOV
    }
}

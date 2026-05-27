package thunder.hack.features.hud.impl;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import thunder.hack.features.hud.HudElement;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

public class CandleHud extends HudElement {
    public CandleHud() {
        super("Candle", 30, 80);
    }

    private Setting<Integer> scale = new Setting<>("Scale", 25, 15, 100);
    private Setting<For> mode = new Setting<>("For", For.Win);

    private final Timer animTimer = new Timer();
    private float flameOffset;

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        if (animTimer.every(50)) {
            flameOffset = (float) (Math.sin(System.currentTimeMillis() / 200.0) * 2);
        }

        float s = scale.getValue();
        float x = getPosX();
        float y = getPosY();

        Color flameColor = switch (mode.getValue()) {
            case Luck -> new Color(255, 215, 0);
            case Win -> new Color(0, 255, 100);
            case LowPing -> new Color(0, 150, 255);
            case AntiKick -> new Color(255, 80, 80);
            case SuperZOV -> new Color(200, 0, 255);
        };

        float flameH = s * 0.6f;
        Render2DEngine.drawRect(context.getMatrices(), x + s * 0.35f, y - flameH + flameOffset, s * 0.3f, flameH, flameColor);
        Render2DEngine.drawRect(context.getMatrices(), x + s * 0.3f, y - flameH * 0.6f + flameOffset, s * 0.4f, flameH * 0.3f, new Color(255, 255, 200, 150));

        int cs = Math.round(s);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TextureStorage.candle,
                Math.round(x), Math.round(y),
                0f, 0f, cs, cs * 2, cs, cs * 2, -1);

        setBounds(x, y - flameH, s, s * 2 + flameH);
    }

    private enum For {
        Luck, Win, LowPing, AntiKick
    }
}

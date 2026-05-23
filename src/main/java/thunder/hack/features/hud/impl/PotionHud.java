package thunder.hack.features.hud.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Formatting;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;

public class PotionHud extends HudElement {
    public PotionHud() {
        super("Potions", 100, 100);
    }

    private float vAnimation, hAnimation;

    private final Setting<Boolean> colored = new Setting<>("Colored", false);

    public static String getDuration(StatusEffectInstance pe) {
        if (pe.isInfinite()) {
            return "*:*";
        } else {
            int var1 = pe.getDuration();
            int mins = var1 / 1200;
            String sec = String.format("%02d", (var1 % 1200) / 20);
            return mins + ":" + sec;
        }
    }

        /*
        Render2DEngine.addWindow(context, getPosX(), getPosY(), getPosX() + hAnimation, getPosY() + vAnimation, 1f);
        for (StatusEffectInstance potionEffect : effects) {
            StatusEffect potion = potionEffect.getEffectType().value();
            String power = "";
            switch (potionEffect.getAmplifier()) {
                case 0 -> power = "I";
                case 1 -> power = "II";
                case 2 -> power = "III";
                case 3 -> power = "IV";
                case 4 -> power = "V";
            }

            String s = potion.getName().getString() + " " + power;
            String s2 = getDuration(potionEffect) + "";

            Color c = new Color(potionEffect.getEffectType().value().getColor());
            FontRenderers.sf_bold_mini.drawString(context.getMatrices(), s + "  " + s2, getPosX() + 5, getPosY() + 20 + y_offset, colored.getValue() ? c.getRGB() : HudEditor.textColor.getValue().getColor());
            y_offset += 10;
        }*/

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}
}
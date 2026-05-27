package thunder.hack.features.hud.impl;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.events.impl.TotemPopEvent;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.combat.AntiBot;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public class Companion extends HudElement {
    public Companion() {
        super("2DCompanion", 50, 10);
    }

    public Setting<Integer> scale = new Setting<>("Scale", 50, 0, 100);
    public Setting<Mode> mode = new Setting<>("Mode", Mode.Boykisser);

    public static int currentFrame;
    private String message = "";
    private Timer lastPop = new Timer();
    private Timer frameRate = new Timer();

    @Override
    public void onUpdate() {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || AntiBot.bots.contains(player) || player.getHealth() > 0 || !Managers.COMBAT.popList.containsKey(player.getName().getString()))
                continue;

            if (isRu())
                message = player.getName().getString() + " попнул " + (Managers.COMBAT.popList.get(player.getName().getString()) > 1 ? Managers.COMBAT.popList.get(player.getName().getString()) + " тотемов и сдох! ИЗЗЗЗИИ" : "тотем и сдох! ИЗЗЗЗИИ");
            else
                message = player.getName().getString() + " popped " + (Managers.COMBAT.popList.get(player.getName().getString()) > 1 ? Managers.COMBAT.popList.get(player.getName().getString()) + " totems and died EZ LMAO!" : "totem and died EZ LMAO!");
            lastPop.reset();
        }
    }

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}


    @EventHandler
    public void onTotemPop(@NotNull TotemPopEvent event) {
        if (event.getEntity() == mc.player) return;

        if (isRu())
            message = event.getEntity().getName().getString() + " попнул " + (event.getPops() > 1 ? event.getPops() + " тотемов!" : "тотем!");
        else
            message = event.getEntity().getName().getString() + " popped " + (event.getPops() > 1 ? event.getPops() + " totems!" : " a totem!");
        lastPop.reset();
    }

    private void drawTextureImmediate(Matrix4f matrix, Identifier tex, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight) {
        // stubbed for 1.21.9
    }

    private enum Mode {
        Boykisser, Paimon, Baltika, Kowk
    }
}

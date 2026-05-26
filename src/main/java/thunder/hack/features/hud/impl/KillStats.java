package thunder.hack.features.hud.impl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Formatting;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.features.modules.combat.AutoCrystal;
import thunder.hack.utility.math.MathUtility;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.TextureStorage;

import java.awt.*;

public class KillStats extends HudElement {
    int death = 0, killstreak = 0, kills = 0;
    public KillStats() {
        super("KillStats",100,35);
    }

    @Override
    public void onDisable() {
        death = 0;
        kills = 0;
        killstreak = 0;
    }

    @EventHandler
    private void death(PacketEvent.Receive event) {
        if(event.getPacket() instanceof EntityStatusS2CPacket pac && pac.getStatus() == 3){
            if(!(pac.getEntity(mc.world) instanceof PlayerEntity)) return;
            if(pac.getEntity(mc.world) == mc.player){
                death++;
                killstreak = 0;
            }
            else if(Aura.target == pac.getEntity(mc.world) || AutoCrystal.target == pac.getEntity(mc.world)){
                killstreak++;
                kills++;
            }
        }
    }

    public void onRender2D(DrawContext context) {
        super.onRender2D(context);

        String streak = "KillStreak: " + Formatting.WHITE + killstreak;
        String kd = " KD: " + Formatting.WHITE + MathUtility.round((float) kills / (death > 0 ? death : 1));
        float pX = getPosX() > mc.getWindow().getScaledWidth() / 2f ? getPosX() - FontRenderers.getModulesRenderer().getStringWidth(streak) - FontRenderers.getModulesRenderer().getStringWidth(kd) : getPosX();

        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), streak, pX, getPosY(), HudEditor.textColor.getValue().getColor());
        FontRenderers.getModulesRenderer().drawString(context.getMatrices(), kd, pX + FontRenderers.getModulesRenderer().getStringWidth(streak), getPosY(), HudEditor.textColor.getValue().getColor());
        setBounds(pX, getPosY(), FontRenderers.getModulesRenderer().getStringWidth(streak) + FontRenderers.getModulesRenderer().getStringWidth(kd), FontRenderers.getModulesRenderer().getFontHeight(streak));
    }
}

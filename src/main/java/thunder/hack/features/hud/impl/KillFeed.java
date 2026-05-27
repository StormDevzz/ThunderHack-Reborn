package thunder.hack.features.hud.impl;

import com.google.common.collect.Lists;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.features.hud.HudElement;
import thunder.hack.features.modules.client.HudEditor;
import thunder.hack.features.modules.combat.Aura;
import thunder.hack.features.modules.combat.AutoCrystal;
import thunder.hack.setting.Setting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.animation.AnimationUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KillFeed extends HudElement {
    public KillFeed() {
        super("KillFeed", 50, 50);
    }

    private Setting<Boolean> resetOnDeath = new Setting<>("ResetOnDeath", true);

    private final List<KillComponent> players = new ArrayList<>();

    private float vAnimation, hAnimation;

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}

    @EventHandler
    public void onPacket(PacketEvent.@NotNull Receive e) {
        if (!(e.getPacket() instanceof EntityStatusS2CPacket pac)) return;
        if (pac.getStatus() == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES && pac.getEntity(mc.world) instanceof PlayerEntity pl) {

            if(pl == mc.player && resetOnDeath.getValue()) {
                players.clear();
                return;
            }

            if ((Aura.target != null && Aura.target == pac.getEntity(mc.world)) || (AutoCrystal.target != null && AutoCrystal.target == pac.getEntity(mc.world))) {
                for (KillComponent kc : Lists.newArrayList(players))
                    if (Objects.equals(kc.getName(), pl.getName().getString())) {
                        kc.increase();
                        return;
                    }
                players.add(new KillComponent(pl.getName().getString()));
            }
        }
    }

    @Override
    public void onDisable() {
        players.clear();
    }

    private class KillComponent {
        private String name;
        private int count;

        public KillComponent(String name) {
            this.name = name;
            this.count = 1;
        }

        public void increase() {
            count++;
        }

        public String getName() {
            return name;
        }

        public String getString() {
            return Formatting.RED + "EZ - " + Formatting.RESET + name + (count > 1 ?  (" [" + Formatting.GRAY + "x" + count + Formatting.RESET + "]") : "");
        }
    }
}

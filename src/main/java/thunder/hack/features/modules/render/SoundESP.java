package thunder.hack.features.modules.render;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;
import thunder.hack.features.modules.Module;
import thunder.hack.gui.font.FontRenderers;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;
import thunder.hack.utility.render.Render2DEngine;
import thunder.hack.utility.render.Render3DEngine;

import java.util.ArrayList;
import java.util.List;

public class SoundESP extends Module {
    public SoundESP() {
        super("SoundESP", Category.RENDER);
    }

    private final Setting<Float> scale = new Setting<>("Scale", 1f, 0.1f, 10f);
    private final Setting<ColorSetting> fillColorA = new Setting<>("Color", new ColorSetting(0x80000000));

    private List<Sound> sounds = new ArrayList<>();

    public void add(double x, double y, double z, String name) {
        sounds.add(new Sound(x, y, z, name.replace("minecraft.block.", "").replace("minecraft.entity", "").replace(".", " ")));
    }

    public void onRender2D(DrawContext context) {
    // stubbed for 1.21.9
}

    @Override
    public void onUpdate() {
        sounds.removeIf(Sound::shouldRemove);
    }

    private class Sound {
        double x, y, z;
        String name;
        int ticks;

        public Sound(double x, double y, double z, String name) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.name = name;
            this.ticks = 60;
        }

        public boolean shouldRemove() {
            return ticks-- <= 0;
        }
    }
}

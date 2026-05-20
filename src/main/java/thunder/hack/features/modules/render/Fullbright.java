package thunder.hack.features.modules.render;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class Fullbright extends Module {
    public Fullbright() {
        super("Fullbright", Category.RENDER);
    }

    public static Setting<Float> minBright = new Setting<>("MinBright", 1f, 0f, 1f);

    private double oldGamma;

    @Override
    public void onEnable() {
        oldGamma = mc.options.getGamma().getValue();
        mc.options.getGamma().setValue(minBright.getValue() * 16.0);
    }

    @Override
    public void onDisable() {
        mc.options.getGamma().setValue(oldGamma);
    }

    @Override
    public void onUpdate() {
        double target = minBright.getValue() * 16.0;
        if (mc.options.getGamma().getValue() != target)
            mc.options.getGamma().setValue(target);
    }
}

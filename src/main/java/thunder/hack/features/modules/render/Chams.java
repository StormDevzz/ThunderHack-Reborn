package thunder.hack.features.modules.render;

import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;

import java.awt.*;

public class Chams extends Module {
    public Chams() {
        super("Chams", Category.RENDER);
    }

    public final Setting<Boolean> crystals = new Setting<>("Crystals", false);
    private final Setting<ColorSetting> crystalColor = new Setting<>("CrystalColor", new ColorSetting(new Color(0x932DD8E8, true)), v -> crystals.getValue());
    private final Setting<Boolean> staticCrystal = new Setting<>("StaticCrystal", true, v -> crystals.getValue());
    private final Setting<CMode> crystalMode = new Setting<>("CrystalMode", CMode.One, v -> crystals.getValue());

    public final Setting<Boolean> players = new Setting<>("Players", false);
    private final Setting<ColorSetting> playerColor = new Setting<>("PlayerColor", new ColorSetting(new Color(0x932DD8E8, true)), v -> players.getValue());
    private final Setting<ColorSetting> friendColor = new Setting<>("FriendColor", new ColorSetting(new Color(0x932DE830, true)), v -> players.getValue());
    private final Setting<Boolean> playerTexture = new Setting<>("PlayerTexture", true, v -> players.getValue());
    private final Setting<Boolean> simple = new Setting<>("Simple", false, v -> players.getValue());

    private final Setting<Boolean> alternativeBlending = new Setting<>("AlternativeBlending", true);

    private enum CMode {
        One, Two, Three
    }

    public void renderCrystal(Object state, Object matrixStack, int i, Object model) {
        // stubbed for 1.21.9
    }

    public void renderPlayer(Object state, Object matrixStack, int light, Object model, Object player) {
        // stubbed for 1.21.9
    }

}
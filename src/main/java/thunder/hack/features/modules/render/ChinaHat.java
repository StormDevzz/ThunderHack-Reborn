package thunder.hack.features.modules.render;

import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.setting.impl.ColorSetting;

import java.awt.*;

public class ChinaHat extends Module {
    public ChinaHat() {
        super("ChinaHat", Category.RENDER);
    }

    private final Setting<Boolean> self = new Setting<>("Self", true);
    private final Setting<Boolean> friends = new Setting<>("Friends", true);
    public final Setting<ColorSetting> color = new Setting<>("Color", new ColorSetting(new Color(0xFFFF4444, true)));

    public boolean shouldRender(PlayerEntity player) {
        if (player == null) return false;
        if (player == mc.player) return self.getValue();
        return friends.getValue() && Managers.FRIEND.isFriend(player);
    }
}

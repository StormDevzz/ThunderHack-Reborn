package thunder.hack.features.modules.render;

//From ThunderHack Plus by VFedTerV
import net.minecraft.entity.player.PlayerEntity;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;

public class ShiftInterp extends Module {
    public ShiftInterp() {
        super("ShiftInterp", Category.RENDER);
    }

    private final Setting<Boolean> everyone = new Setting<>("Everyone", true);
    private final Setting<Boolean> enemies = new Setting<>("Enemies", false);
    private final Setting<Boolean> friends = new Setting<>("Friends", false);
    private final Setting<Boolean> self = new Setting<>("Self", false);

    public boolean shouldShift(PlayerEntity player) {
        if (player == null) return false;

        if (everyone.getValue()) return true;
        if (self.getValue() && player == mc.player) return true;
        if (enemies.getValue() && !Managers.FRIEND.isFriend(player.getName().getString()) && player != mc.player) return true;
        if (friends.getValue() && Managers.FRIEND.isFriend(player.getName().getString())) return true;

        return false;
    }
}
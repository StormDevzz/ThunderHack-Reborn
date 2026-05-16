package thunder.hack.features.modules.misc;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.Timer;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.CookieStorage;
import java.util.Map;

public class AutoReconnect extends Module {
    public AutoReconnect() {
        super("AutoReconnect", Category.MISC);
    }

    private final Setting<Integer> delay = new Setting<>("Delay", 5, 1, 30);
    private final Setting<Boolean> auto = new Setting<>("Auto", false);

    private ServerInfo lastServer;

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    public int getDelay() {
        return delay.getValue();
    }

    public boolean isAuto() {
        return auto.getValue();
    }

    public void reconnect(Screen parent) {
        if (lastServer != null) {
            ServerAddress address = ServerAddress.parse(lastServer.address);
            ConnectScreen.connect(
                    parent != null ? parent : new MultiplayerScreen(new TitleScreen()),
                    mc,
                    address,
                    lastServer,
                    false,
                    new CookieStorage(Map.of())
            );
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent e) {
        if (mc.getCurrentServerEntry() != null) {
            lastServer = mc.getCurrentServerEntry();
        }
    }
}
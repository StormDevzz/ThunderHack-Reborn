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

import net.minecraft.client.network.CookieStorage;
import java.util.Map;
 
public class AutoReconnect extends Module {
    public AutoReconnect() {
        super("AutoReconnect", Category.MISC);
    }

    private final Setting<Integer> delay = new Setting<>("Delay", 5, 1, 30);
    private final Setting<Boolean> auto = new Setting<>("Auto", false);

    private Timer reconnectTimer = new Timer();
    private boolean reconnecting = false;
    private ServerInfo lastServer;

    @Override
    public void onEnable() {
        reconnecting = false;
        reconnectTimer.reset();
    }

    @Override
    public void onDisable() {
        reconnecting = false;
    }

    public int getDelay() {
        return delay.getValue();
    }

    public void reconnect() {
        if (lastServer != null) {
            ServerAddress address = ServerAddress.parse(lastServer.address);
            ConnectScreen.connect(
                    new MultiplayerScreen(new TitleScreen()), // родительский экран
                    mc, // клиент
                    address, // адрес сервера
                    lastServer, // информация о сервере
                    false, // quickPlay (обычно false)
                    new CookieStorage(Map.of()) // куки
            );
        }
    }

    public void scheduleReconnect() {
        reconnectTimer.reset();
        reconnecting = true;
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent e) {
        if (mc.getCurrentServerEntry() != null) {
            lastServer = mc.getCurrentServerEntry();
        }

        if (auto.getValue() && mc.currentScreen instanceof DisconnectedScreen && !reconnecting) {
            reconnectTimer.reset();
            reconnecting = true;
        }

        if (reconnecting && reconnectTimer.passedMs(delay.getValue() * 1000L)) {
            reconnect();
            reconnecting = false;
        }

        if (!(mc.currentScreen instanceof DisconnectedScreen)) {
            reconnecting = false;
        }
    }
}
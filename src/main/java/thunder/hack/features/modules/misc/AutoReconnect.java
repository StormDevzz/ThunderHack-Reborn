package thunder.hack.features.modules.misc;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import meteordevelopment.orbit.EventHandler;
import thunder.hack.events.impl.PlayerUpdateEvent;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class AutoReconnect extends Module {
    public AutoReconnect() {
        super("AutoReconnect", Category.MISC);
    }

    private final Setting<Integer> delay = new Setting<>("Delay", 5, 1, 60);
    private final Setting<Boolean> auto = new Setting<>("Auto", true);
    public final Setting<Boolean> buttonInGameMenu = new Setting<>("ButtonInGameMenu", true);

    private String lastAddress = null;
    private String lastServerName = null;

    public int getDelay() { return delay.getValue(); }
    public boolean isAuto() { return auto.getValue(); }
    public boolean hasServer() { return lastAddress != null; }
    public String getLastAddress() { return lastAddress; }

    /**
     * Принудительно отключает игрока от сервера (если онлайн)
     * и подключает заново через стандартный ConnectScreen.
     * null для CookieStorage — это обязательно, иначе "Server does not accept transfers".
     */
    public void forceReconnect() {
        if (lastAddress == null) return;

        String savedAddress = lastAddress;
        String savedName = lastServerName != null ? lastServerName : lastAddress;

        mc.execute(() -> {
            // Принудительно разрываем соединение если оно есть
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().getConnection().disconnect(Text.literal("Reconnecting..."));
            }

            // Небольшая задержка чтобы соединение успело закрыться
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}

                mc.execute(() -> {
                    ServerInfo freshInfo = new ServerInfo(savedName, savedAddress, ServerInfo.ServerType.OTHER);
                    ServerAddress serverAddress = ServerAddress.parse(savedAddress);
                    Screen returnScreen = new MultiplayerScreen(new TitleScreen());

                    ConnectScreen.connect(returnScreen, mc, serverAddress, freshInfo, false, null);
                });
            }, "TH-Reconnect").start();
        });
    }

    // Вызывается с DisconnectedScreen (уже отключены — без форс-дисконнекта)
    public void reconnect(Screen parent) {
        if (lastAddress == null) return;

        String savedAddress = lastAddress;
        String savedName = lastServerName != null ? lastServerName : lastAddress;

        ServerInfo freshInfo = new ServerInfo(savedName, savedAddress, ServerInfo.ServerType.OTHER);
        ServerAddress serverAddress = ServerAddress.parse(savedAddress);
        Screen returnScreen = parent != null ? parent : new MultiplayerScreen(new TitleScreen());

        ConnectScreen.connect(returnScreen, mc, serverAddress, freshInfo, false, null);
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent e) {
        if (mc.getCurrentServerEntry() != null) {
            lastAddress = mc.getCurrentServerEntry().address;
            lastServerName = mc.getCurrentServerEntry().name;
        }
    }
}
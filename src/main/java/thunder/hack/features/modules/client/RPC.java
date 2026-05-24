package thunder.hack.features.modules.client;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import thunder.hack.ThunderHack;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.client.ModuleManager;
import thunder.hack.features.modules.Module;
import thunder.hack.setting.Setting;
import thunder.hack.utility.ThunderUtility;
import thunder.hack.utility.Timer;
import thunder.hack.utility.discord.DiscordRPC;
import thunder.hack.utility.discord.DiscordRichPresence;

import java.io.*;
import java.util.Objects;

import static thunder.hack.features.modules.client.ClientSettings.isRu;

public final class RPC extends Module {
    private static final DiscordRPC rpc = DiscordRPC.INSTANCE;

    public static Setting<RPCMode> rpcMode = new Setting<>("RPC Mode", RPCMode.Old);

    public static Setting<Mode> mode = new Setting<>("Picture", Mode.Reborn, v -> rpcMode.getValue() == RPCMode.Old);
    public static Setting<Boolean> showIP = new Setting<>("ShowIP", true, v -> rpcMode.getValue() == RPCMode.Old);
    public static Setting<sMode> smode = new Setting<>("StateMode", sMode.Stats, v -> rpcMode.getValue() == RPCMode.Old);
    public static Setting<String> state = new Setting<>("State", "Beta? Reborn? NextGen?", v -> rpcMode.getValue() == RPCMode.Old);
    public static Setting<Boolean> nickname = new Setting<>("Nickname", true, v -> rpcMode.getValue() == RPCMode.Old);

    public static Setting<Boolean> nServer = new Setting<>("ShowServer", true, v -> rpcMode.getValue() == RPCMode.New);
    public static Setting<Boolean> nNick = new Setting<>("ShowNick", true, v -> rpcMode.getValue() == RPCMode.New);
    public static Setting<Boolean> nBuild = new Setting<>("ShowBuild", true, v -> rpcMode.getValue() == RPCMode.New);
    public static Setting<Boolean> nVersion = new Setting<>("ShowVersion", true, v -> rpcMode.getValue() == RPCMode.New);
    public static Setting<Boolean> nHandshake = new Setting<>("ShowHandshake", true, v -> rpcMode.getValue() == RPCMode.New);
    public static Setting<String> nDesc = new Setting<>("CustomDesc", "ThunderHack Reborn", v -> rpcMode.getValue() == RPCMode.New);
    public static Setting<Boolean> nAutoState = new Setting<>("AutoStatus", true, v -> rpcMode.getValue() == RPCMode.New);
    public static Setting<String> nState = new Setting<>("CustomStatus", "Pasting...", v -> rpcMode.getValue() == RPCMode.New && !nAutoState.getValue());
    public static Setting<Boolean> nUseCustomRotation = new Setting<>("UseCustomRotation", false, v -> rpcMode.getValue() == RPCMode.New && nAutoState.getValue());
    public static Setting<String> nCustomStatuses = new Setting<>("CustomStatuses", "pasting;coding;chilling", v -> rpcMode.getValue() == RPCMode.New && nAutoState.getValue() && nUseCustomRotation.getValue());
    public static Setting<NewImageType> nImageType = new Setting<>("Image Type", NewImageType.Asset, v -> rpcMode.getValue() == RPCMode.New);
    public static Setting<NewImage> nImage = new Setting<>("Image", NewImage.Default, v -> rpcMode.getValue() == RPCMode.New && nImageType.getValue() == NewImageType.Asset);
    public static Setting<GifChoice> nGif = new Setting<>("GIF", GifChoice.Reborn, v -> rpcMode.getValue() == RPCMode.New && nImageType.getValue() == NewImageType.Gif);

    public static DiscordRichPresence presence = new DiscordRichPresence();
    public static boolean started;
    public static final String BUILD_VERSION;
    static {
        String v = ThunderUtility.readManifestField("Implementation-Version");
        BUILD_VERSION = v.equals("0") ? ThunderHack.VERSION : v;
    }
    static String String1 = "none";
    private final Timer timer_delay = new Timer();
    private final Timer nTimer = new Timer();
    private static Thread thread;
    private static String currentId = "";

    String slov;
    String nSlov;
    String[] rpc_perebor_en = {"Parkour", "Reporting cheaters", "Touching grass", "Asks how to bind", "Reporting bugs", "Watching Kilab"};
    String[] rpc_perebor_ru = {"Паркурит", "Репортит читеров", "Трогает траву", "Спрашивает как забиндить", "Репортит баги", "Смотрит Флюгера"};

    String[] rpc_new_en = {"pasting", "making coffee", "smoking grass", "having fun", "idk", "breaking everything", "coding", "resting", "testing", "chilling", "exploring", "building", "farming", "raiding", "hunting"};
    String[] rpc_new_ru = {"пастит", "делает кофеек", "травку курит", "развлекается", "хз", "ломает всё", "кодит", "отдыхает", "тестит", "чиллит", "исследует", "строит", "фермит", "рейдит", "охотится"};

    int randomInt;
    int nRandomInt;

    public RPC() {
        super("DiscordRPC", Category.CLIENT);
    }

    public static void readFile() {
        try {
            File file = new File("ThunderHackReborn/misc/RPC.txt");
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    while (reader.ready()) {
                        String1 = reader.readLine();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void WriteFile(String url1, String url2) {
        File file = new File("ThunderHackReborn/misc/RPC.txt");
        try {
            file.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(url1 + "SEPARATOR" + url2 + '\n');
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDisable() {
        started = false;
        currentId = "";
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        rpc.Discord_Shutdown();
    }

    @Override
    public void onUpdate() {
        startRpc();
    }

    public void startRpc() {
        if (isDisabled()) return;

        String targetId = rpcMode.getValue() == RPCMode.New ? "1505321724839858186" : "1093053626198523935";

        if (started && !currentId.equals(targetId)) {
            onDisable();
            started = false;
        }

        if (!started) {
            started = true;
            currentId = targetId;
            rpc.Discord_Initialize(targetId, true, "");
            presence.startTimestamp = (System.currentTimeMillis() / 1000L);
            presence.largeImageText = "v" + BUILD_VERSION + "  ·  " + ThunderHack.GITHUB_HASH;
            rpc.Discord_UpdatePresence(presence);

            thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    rpc.Discord_RunCallbacks();

                    if (rpcMode.getValue() == RPCMode.Old) {
                        presence.details = getDetails();
                        switch (smode.getValue()) {
                            case Stats ->
                                    presence.state = "Hacks: " + Managers.MODULE.getEnabledModules().size() + " / " + Managers.MODULE.modules.size();
                            case Custom -> presence.state = state.getValue();
                            case Version -> presence.state = "v" + BUILD_VERSION + " for mc 1.21";
                        }

                        if (nickname.getValue()) {
                            presence.smallImageText = "logged as - " + mc.getSession().getUsername();
                            presence.smallImageKey = "https://minotar.net/helm/" + mc.getSession().getUsername() + "/100.png";
                        } else {
                            presence.smallImageText = "";
                            presence.smallImageKey = "";
                        }

                        switch (mode.getValue()) {
                            case Reborn -> presence.largeImageKey = "https://i.imgur.com/yY0z2Uq.gif";
                            case MegaCute ->
                                    presence.largeImageKey = "https://media1.tenor.com/images/6bcbfcc0be97d029613b54f97845bc59/tenor.gif?itemid=26823781";
                            case Custom -> {
                                readFile();
                                presence.largeImageKey = String1.split("SEPARATOR")[0];
                                if (!Objects.equals(String1.split("SEPARATOR")[1], "none")) {
                                    presence.smallImageKey = String1.split("SEPARATOR")[1];
                                }
                            }
                        }
                    } else {
                        String status;
                        if (nAutoState.getValue()) {
                            if (nTimer.passedMs(60 * 1000) || nSlov == null) {
                                if (nUseCustomRotation.getValue()) {
                                    String[] customList = nCustomStatuses.getValue().split(";");
                                    nSlov = customList.length > 0
                                        ? customList[(int) (Math.random() * customList.length)]
                                        : "ThunderHack";
                                } else {
                                    nRandomInt = (int) (Math.random() * rpc_new_en.length);
                                    nSlov = isRu() ? rpc_new_ru[nRandomInt] : rpc_new_en[nRandomInt];
                                }
                                nTimer.reset();
                            }
                            status = nSlov;
                        } else {
                            status = nState.getValue();
                        }

                        String server = mc.isInSingleplayer()
                            ? "SinglePlayer"
                            : mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Menu";

                        StringBuilder detailsBuilder = new StringBuilder(status);
                        if (nServer.getValue()) {
                            detailsBuilder.append("  |  ").append(server);
                        }
                        presence.details = detailsBuilder.toString();

                        StringBuilder stateBuilder = new StringBuilder(nDesc.getValue());
                        if (nHandshake.getValue()) {
                            String brand = ModuleManager.clientSpoof.getClientName();
                            if (brand != null)
                                stateBuilder.append("  ·  ").append(brand);
                        }
                        presence.state = stateBuilder.toString();

                        StringBuilder hoverBuilder = new StringBuilder();
                        if (nVersion.getValue())
                            hoverBuilder.append("v").append(BUILD_VERSION);
                        if (nBuild.getValue())
                            hoverBuilder.append(hoverBuilder.length() > 0 ? "  ·  " : "").append(ThunderHack.GITHUB_HASH);
                        if (nNick.getValue())
                            hoverBuilder.append(hoverBuilder.length() > 0 ? "  ·  " : "").append(mc.getSession().getUsername());

                        presence.largeImageText = hoverBuilder.length() > 0 ? hoverBuilder.toString() : "ThunderHack";

                        if (nImageType.getValue() == NewImageType.Asset) {
                            if (nImage.getValue() == NewImage.Default)
                                presence.largeImageKey = "logo";
                            else
                                presence.largeImageKey = nImage.getValue().getName();
                        } else {
                            switch (nGif.getValue()) {
                                case Reborn -> presence.largeImageKey = "https://i.imgur.com/yY0z2Uq.gif";
                                case MegaCute -> presence.largeImageKey = "https://media1.tenor.com/images/6bcbfcc0be97d029613b54f97845bc59/tenor.gif?itemid=26823781";
                                case CPvP -> presence.largeImageKey = "https://media1.tenor.com/m/32pzMbItFo0AAAAd/2b2t-virgin.gif";
                                case Bald -> presence.largeImageKey = "https://media1.tenor.com/m/LAsK1qXceVwAAAAC/7zqs-fitmc.gif";
                                case FuckingHell -> presence.largeImageKey = "https://media1.tenor.com/m/1gTUDBhsOQoAAAAd/hell-sent-to-hell.gif";
                                case Jocker -> presence.largeImageKey = "https://media1.tenor.com/m/YVP1R3hLzIMAAAAC/bachibachbach-2b2t.gif";
                                case Femboy -> presence.largeImageKey = "https://media.tenor.com/jbWSVDF4jD0AAAAM/2b2t-femboy.gif";
                                case Zdarova -> presence.largeImageKey = "https://media1.tenor.com/m/2UjViO0RunsAAAAC/arwyx-2b2t.gif";
                                case Secret -> presence.largeImageKey = "https://media1.tenor.com/m/a2RLe4oaCuMAAAAC/wynncraft-skyblock.gif";
                                case PoopBoob -> presence.largeImageKey = "https://media.tenor.com/yKpYBTEnPakAAAAi/popbob-2b2t.gif";
                            }
                        }

                        presence.smallImageKey = "";
                        presence.smallImageText = "";
                    }

                    presence.button_label_1 = "Download";
                    presence.button_url_1 = "https://github.com/StormDevzz/ThunderHack-Reborn/";

                    rpc.Discord_UpdatePresence(presence);
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "TH-RPC-Handler");
            thread.start();
        }
    }

    private String getDetails() {
        String result = "";

        if (mc.currentScreen instanceof MultiplayerScreen || mc.currentScreen instanceof AddServerScreen || mc.currentScreen instanceof TitleScreen) {
            if (timer_delay.passedMs(60 * 1000)) {
                randomInt = (int) (Math.random() * rpc_perebor_en.length);
                slov = isRu() ? rpc_perebor_ru[randomInt] : rpc_perebor_en[randomInt];
                timer_delay.reset();
            }
            result = slov;
        } else if (mc.getCurrentServerEntry() != null) {
            result = isRu() ? (showIP.getValue() ? "Играет на " + mc.getCurrentServerEntry().address : "Играет на сервере") : (showIP.getValue() ? "Playing on " + mc.getCurrentServerEntry().address : "Playing on server");
        } else if (mc.isInSingleplayer()) {
            result = isRu() ? "Читерит в одиночке" : "SinglePlayer hacker";
        }
        return result;
    }

    public enum RPCMode {Old, New}

    public enum Mode {Custom, MegaCute, Reborn}

    public enum sMode {Custom, Stats, Version}

    public enum NewImage {
        Default("default"),
        Kotost("kotost"),
        Cuute("cuute"),
        Cutefurry("cutefurry"),
        Forreal("forreal"),
        Furry1("furry1"),
        Furry2("furry2"),
        Ebat("ebat"),
        Oooh("oooh"),
        Pivo("pivo"),
        Whenmem("whenmem"),
        Ninja("ninja"),
        Navalniylexa("navalniylexa"),
        Depression("depression"),
        Zeleboba("zeleboba"),
        Akrien("akrien"),
        Zeleboba1("zeleboba1"),
        Nihua("nihua"),
        Kotost1("kotost1"),
        Vanya("vanya"),
        Pyramids("pyramids"),
        Vozduhan("vozduhan"),
        Hvh("hvh"),
        Standoff2("standoff2"),
        Dog("dog"),
        Tree("tree"),
        Vitya("vitya"),
        Fuck("fuck"),
        Wypher("wypher"),
        Wypher1("wypher1"),
        Pretty("pretty");

        private final String name;
        NewImage(String name) { this.name = name; }
        public String getName() { return name; }
    }

    public enum NewImageType {Asset, Gif}

    public enum GifChoice {Reborn, MegaCute, CPvP, Bald, FuckingHell, Jocker, Femboy, Zdarova, Secret, PoopBoob}
}

package thunder.hack.core.manager.client;

import meteordevelopment.orbit.EventHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.UnknownCustomPayload;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Identifier;
import thunder.hack.core.manager.IManager;
import thunder.hack.events.impl.PacketEvent;
import thunder.hack.utility.Timer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginManager implements IManager {
    private String serverBrand = "Unknown";
    private final Set<String> detectedPlugins = new LinkedHashSet<>();
    private final Set<Identifier> passiveChannels = new LinkedHashSet<>();
    private String rawResponse = null;
    private boolean awaitingResponse = false;
    private final Timer requestTimer = new Timer();
    private boolean hasQueried = false;

    private static final Pattern PLUGIN_LIST_REGEX =
            Pattern.compile("(?i)(?:plugins|плагины)\\s*\\((\\d+)\\)\\s*:\\s*(.+)");
    private static final Pattern PERMISSION_ERROR =
            Pattern.compile("(?i)(unknown command|unknown or incomplete|permission|not allowed|no access|you don't have|you are not)");

    private static final Map<String, String> CHANNEL_TO_PLUGIN = new LinkedHashMap<>();
    private static final Map<String, String> BRAND_SOFTWARE = new LinkedHashMap<>();

    static {
        CHANNEL_TO_PLUGIN.put("bungeecord", "BungeeCord");
        CHANNEL_TO_PLUGIN.put("velocity", "Velocity");
        CHANNEL_TO_PLUGIN.put("essentials", "EssentialsX");
        CHANNEL_TO_PLUGIN.put("worldedit", "WorldEdit");
        CHANNEL_TO_PLUGIN.put("worldguard", "WorldGuard");
        CHANNEL_TO_PLUGIN.put("authme", "AuthMe");
        CHANNEL_TO_PLUGIN.put("cmi", "CMI");
        CHANNEL_TO_PLUGIN.put("luckperms", "LuckPerms");
        CHANNEL_TO_PLUGIN.put("viaversion", "ViaVersion");
        CHANNEL_TO_PLUGIN.put("geyser", "Geyser");
        CHANNEL_TO_PLUGIN.put("floodgate", "Floodgate");
        CHANNEL_TO_PLUGIN.put("skript", "Skript");
        CHANNEL_TO_PLUGIN.put("mcmmo", "mcMMO");
        CHANNEL_TO_PLUGIN.put("placeholderapi", "PlaceholderAPI");
        CHANNEL_TO_PLUGIN.put("packetevents", "PacketEvents");
        CHANNEL_TO_PLUGIN.put("factions", "Factions");
        CHANNEL_TO_PLUGIN.put("towny", "Towny");
        CHANNEL_TO_PLUGIN.put("griefprevention", "GriefPrevention");
        CHANNEL_TO_PLUGIN.put("coreprotect", "CoreProtect");
        CHANNEL_TO_PLUGIN.put("advancedban", "AdvancedBan");
        CHANNEL_TO_PLUGIN.put("dynmap", "Dynmap");
        CHANNEL_TO_PLUGIN.put("pl3xmap", "Pl3xMap");
        CHANNEL_TO_PLUGIN.put("minetp", "MineTP");
        CHANNEL_TO_PLUGIN.put("lpb", "LibertyBans");
        CHANNEL_TO_PLUGIN.put("acr", "AntiCrystalReloaded");
        CHANNEL_TO_PLUGIN.put("plan", "Plan");
        CHANNEL_TO_PLUGIN.put("nucleus", "Nucleus");
        CHANNEL_TO_PLUGIN.put("gml", "GMLimbo");

        BRAND_SOFTWARE.put("purpur", "Purpur");
        BRAND_SOFTWARE.put("paper", "Paper");
        BRAND_SOFTWARE.put("spigot", "Spigot");
        BRAND_SOFTWARE.put("fabric", "Fabric");
        BRAND_SOFTWARE.put("quilt", "Quilt");
        BRAND_SOFTWARE.put("mohist", "Mohist");
        BRAND_SOFTWARE.put("catserver", "CatServer");
        BRAND_SOFTWARE.put("arclight", "Arclight");
        BRAND_SOFTWARE.put("magma", "Magma");
        BRAND_SOFTWARE.put("folia", "Folia");
        BRAND_SOFTWARE.put("velocity", "Velocity (Proxy)");
        BRAND_SOFTWARE.put("bungeecord", "BungeeCord (Proxy)");
        BRAND_SOFTWARE.put("bungee", "BungeeCord (Proxy)");
        BRAND_SOFTWARE.put("waterfall", "Waterfall (Proxy)");
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof CustomPayloadS2CPacket cp) {
            CustomPayload payload = cp.payload();
            if (payload instanceof BrandCustomPayload brand) {
                serverBrand = brand.brand();
            } else if (payload instanceof UnknownCustomPayload unknown) {
                Identifier id = unknown.id();
                if (passiveChannels.add(id)) {
                    if (!id.getNamespace().equals("minecraft")) {
                        String plugin = CHANNEL_TO_PLUGIN.get(id.getNamespace());
                        if (plugin != null) detectedPlugins.add(plugin);
                    }
                }
            }
        }

        if (event.getPacket() instanceof GameJoinS2CPacket) {
            reset();
        }

        if (event.getPacket() instanceof GameMessageS2CPacket msg) {
            handleChatMessage(msg.content().getString());
        }
    }

    private void handleChatMessage(String text) {
        if (!awaitingResponse || requestTimer.passedMs(5000)) {
            awaitingResponse = false;
            return;
        }

        Matcher matcher = PLUGIN_LIST_REGEX.matcher(text);
        if (matcher.find()) {
            String list = matcher.group(2);
            for (String name : list.split(",\\s*")) {
                String clean = name.replaceAll("\\s+v?[\\d.]+.*$", "").trim();
                if (!clean.isEmpty()) detectedPlugins.add(clean);
            }
            rawResponse = text;
            awaitingResponse = false;
            hasQueried = true;
            return;
        }

        if (PERMISSION_ERROR.matcher(text).find()) {
            rawResponse = text;
            awaitingResponse = false;
            hasQueried = true;
            return;
        }

        if (text.toLowerCase().contains("plugins") || text.toLowerCase().contains("плагины")) {
            rawResponse = text;
            awaitingResponse = false;
            hasQueried = true;
        }
    }

    public void requestPluginList() {
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        detectedPlugins.clear();
        rawResponse = null;

        mc.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("plugins"));
        awaitingResponse = true;
        hasQueried = false;
        requestTimer.reset();

        scanFabricChannels();
    }

    private void scanFabricChannels() {
        try {
            Set<Identifier> sendable = ClientPlayNetworking.getSendable();
            for (Identifier ch : sendable) {
                passiveChannels.add(ch);
                if (!ch.getNamespace().equals("minecraft")) {
                    String plugin = CHANNEL_TO_PLUGIN.get(ch.getNamespace());
                    if (plugin != null) detectedPlugins.add(plugin);
                    else detectedPlugins.add("?" + ch.toString());
                }
            }
        } catch (Exception ignored) {}

        try {
            ClientPlayNetworking.getReceived().stream()
                    .filter(ch -> !ch.getNamespace().equals("minecraft"))
                    .forEach(ch -> {
                        passiveChannels.add(ch);
                        String plugin = CHANNEL_TO_PLUGIN.get(ch.getNamespace());
                        if (plugin != null) detectedPlugins.add(plugin);
                        else detectedPlugins.add("?" + ch.toString());
                    });
        } catch (Exception ignored) {}
    }

    public String getServerBrand() { return serverBrand; }

    public String getServerSoftware() {
        String low = serverBrand.toLowerCase();
        for (Map.Entry<String, String> e : BRAND_SOFTWARE.entrySet())
            if (low.contains(e.getKey())) return e.getValue();
        return "Unknown/Custom";
    }

    public Set<String> getDetectedPlugins() {
        scanFabricChannels();
        return detectedPlugins;
    }

    public Set<Identifier> getDetectedChannels() {
        scanFabricChannels();
        return passiveChannels;
    }

    public String getRawResponse() { return rawResponse; }
    public boolean hasQueried() { return hasQueried; }

    private void reset() {
        serverBrand = "Unknown";
        detectedPlugins.clear();
        passiveChannels.clear();
        rawResponse = null;
        awaitingResponse = false;
        hasQueried = false;
    }
}

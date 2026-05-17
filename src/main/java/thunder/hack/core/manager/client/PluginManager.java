package thunder.hack.core.manager.client;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.UnknownCustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.Identifier;
import thunder.hack.core.manager.IManager;
import thunder.hack.events.impl.PacketEvent;

import java.util.*;

public class PluginManager implements IManager {
    private String serverBrand = "Unknown";
    private final Set<Identifier> detectedChannels = new HashSet<>();

    private static final Map<String, String> CHANNEL_TO_PLUGIN = new LinkedHashMap<>();

    static {
        CHANNEL_TO_PLUGIN.put("bungeecord", "BungeeCord");
        CHANNEL_TO_PLUGIN.put("velocity", "Velocity");
        CHANNEL_TO_PLUGIN.put("essentials", "EssentialsX");
        CHANNEL_TO_PLUGIN.put("worldedit", "WorldEdit");
        CHANNEL_TO_PLUGIN.put("worldguard", "WorldGuard");
        CHANNEL_TO_PLUGIN.put("authme", "AuthMe");
        CHANNEL_TO_PLUGIN.put("loginsen", "LoginSecurity");
        CHANNEL_TO_PLUGIN.put("cmi", "CMI");
        CHANNEL_TO_PLUGIN.put("luckperms", "LuckPerms");
        CHANNEL_TO_PLUGIN.put("viaversion", "ViaVersion");
        CHANNEL_TO_PLUGIN.put("geyser", "Geyser");
        CHANNEL_TO_PLUGIN.put("floodgate", "Floodgate");
        CHANNEL_TO_PLUGIN.put("skript", "Skript");
        CHANNEL_TO_PLUGIN.put("mcmmo", "mcMMO");
        CHANNEL_TO_PLUGIN.put("placeholderapi", "PlaceholderAPI");
        CHANNEL_TO_PLUGIN.put("packetevents", "PacketEvents");
        CHANNEL_TO_PLUGIN.put("minetp", "MineTP");
        CHANNEL_TO_PLUGIN.put("factions", "Factions");
        CHANNEL_TO_PLUGIN.put("towny", "Towny");
        CHANNEL_TO_PLUGIN.put("griefprevention", "GriefPrevention");
        CHANNEL_TO_PLUGIN.put("coreprotect", "CoreProtect");
        CHANNEL_TO_PLUGIN.put("advancedban", "AdvancedBan");
        CHANNEL_TO_PLUGIN.put("lpb", "LibertyBans");
        CHANNEL_TO_PLUGIN.put("anticheatreloaded", "AntiCheatReloaded");
        CHANNEL_TO_PLUGIN.put("ncp", "NoCheatPlus");
        CHANNEL_TO_PLUGIN.put("acr", "AntiCrystalReloaded");
        CHANNEL_TO_PLUGIN.put("bedrock", "Geyser");
        CHANNEL_TO_PLUGIN.put("dynmap", "Dynmap");
        CHANNEL_TO_PLUGIN.put("pl3xmap", "Pl3xMap");
        CHANNEL_TO_PLUGIN.put("blueprint", "BlueMap");
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof CustomPayloadS2CPacket cp) {
            CustomPayload payload = cp.payload();
            if (payload instanceof BrandCustomPayload brand) {
                serverBrand = brand.brand();
            } else if (payload instanceof UnknownCustomPayload unknown) {
                detectedChannels.add(unknown.id());
            }
        }

        if (event.getPacket() instanceof GameJoinS2CPacket) {
            reset();
        }
    }

    public String getServerBrand() {
        return serverBrand;
    }

    public String getServerSoftware() {
        String brand = serverBrand.toLowerCase();
        if (brand.contains("purpur")) return "Purpur";
        if (brand.contains("paper")) return "Paper";
        if (brand.contains("spigot")) return "Spigot";
        if (brand.contains("fabric")) return "Fabric";
        if (brand.contains("quilt")) return "Quilt";
        if (brand.contains("mohist")) return "Mohist";
        if (brand.contains("catserver")) return "CatServer";
        if (brand.contains("arclight")) return "Arclight";
        if (brand.contains("magma")) return "Magma";
        if (brand.contains("crucible")) return "Crucible";
        if (brand.contains("folia")) return "Folia";
        if (brand.contains("velocity")) return "Velocity (Proxy)";
        if (brand.contains("bungeecord") || brand.contains("bungee")) return "BungeeCord (Proxy)";
        if (brand.contains("waterfall")) return "Waterfall (Proxy)";
        return "Unknown";
    }

    public Set<Identifier> getDetectedChannels() {
        return detectedChannels;
    }

    public Set<String> getDetectedPlugins() {
        Set<String> plugins = new LinkedHashSet<>();
        for (Identifier channel : detectedChannels) {
            String namespace = channel.getNamespace();
            String plugin = CHANNEL_TO_PLUGIN.get(namespace);
            if (plugin != null) plugins.add(plugin);
        }
        return plugins;
    }

    public Set<Identifier> getUnknownChannels() {
        Set<Identifier> unknown = new LinkedHashSet<>();
        for (Identifier channel : detectedChannels) {
            if (!CHANNEL_TO_PLUGIN.containsKey(channel.getNamespace())) {
                unknown.add(channel);
            }
        }
        return unknown;
    }

    public void reset() {
        serverBrand = "Unknown";
        detectedChannels.clear();
    }
}

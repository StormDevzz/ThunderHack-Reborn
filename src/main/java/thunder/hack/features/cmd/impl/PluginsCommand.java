package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.features.cmd.Command;

import java.util.Set;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class PluginsCommand extends Command {
    public PluginsCommand() {
        super("plugins");
    }

    @Override
    public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            boolean hadData = !Managers.PLUGIN.getDetectedPlugins().isEmpty();

            sendMessage(Formatting.GOLD + "Server: " + Formatting.WHITE + Managers.PLUGIN.getServerBrand());
            sendMessage(Formatting.GOLD + "Software: " + Formatting.WHITE + Managers.PLUGIN.getServerSoftware());

            Set<String> plugins = Managers.PLUGIN.getDetectedPlugins();
            Set<Identifier> channels = Managers.PLUGIN.getDetectedChannels();
            String raw = Managers.PLUGIN.getRawResponse();

            if (!plugins.isEmpty()) {
                sendMessage(Formatting.GOLD + "Plugins (" + plugins.size() + "):");
                for (String p : plugins) {
                    String color = p.startsWith("?") ? Formatting.DARK_GRAY.toString() : Formatting.GREEN.toString();
                    sendMessage(color + "  \u2713 " + Formatting.WHITE + p);
                }
            }

            if (!channels.isEmpty()) {
                sendMessage(Formatting.GOLD + "Channels (" + channels.size() + "):");
                for (Identifier ch : channels) {
                    String color = ch.getNamespace().equals("minecraft")
                            ? Formatting.DARK_GRAY.toString()
                            : Formatting.GRAY.toString();
                    sendMessage(color + "  " + ch.toString());
                }
            }

            if (raw != null) {
                sendMessage(Formatting.GOLD + "Raw /plugins:");
                sendMessage(Formatting.GRAY + "  " + raw);
            }

            if (!hadData) {
                sendMessage(Formatting.YELLOW + "Sending /plugins request...");
                Managers.PLUGIN.requestPluginList();
                sendMessage(Formatting.GRAY + "Run @plugins again to see results");
            } else {
                sendMessage(Formatting.GRAY + "Run @plugins to re-query");
                Managers.PLUGIN.requestPluginList();
            }

            return SINGLE_SUCCESS;
        });
    }
}

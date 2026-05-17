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
            String brand = Managers.PLUGIN.getServerBrand();
            String software = Managers.PLUGIN.getServerSoftware();
            Set<String> plugins = Managers.PLUGIN.getDetectedPlugins();
            Set<Identifier> unknown = Managers.PLUGIN.getUnknownChannels();

            sendMessage(Formatting.GOLD + "Server: " + Formatting.WHITE + brand);
            sendMessage(Formatting.GOLD + "Software: " + Formatting.WHITE + software);

            if (!plugins.isEmpty()) {
                sendMessage(Formatting.GOLD + "Detected plugins:");
                for (String p : plugins) {
                    sendMessage(Formatting.GREEN + "  \u2713 " + Formatting.WHITE + p);
                }
            } else {
                sendMessage(Formatting.YELLOW + "No plugins detected yet");
                sendMessage(Formatting.GRAY + "(Plugin channels appear when plugins send data)");
            }

            if (!unknown.isEmpty()) {
                sendMessage(Formatting.GOLD + "Unknown channels:");
                for (Identifier ch : unknown) {
                    sendMessage(Formatting.DARK_GRAY + "  " + ch.toString());
                }
            }

            return SINGLE_SUCCESS;
        });
    }
}

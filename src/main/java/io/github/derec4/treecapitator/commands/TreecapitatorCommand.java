package io.github.derec4.treecapitator.commands;

import io.github.derec4.treecapitator.TreecapitatorPlugin;
import io.github.derec4.treecapitator.listeners.BlockBreakListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TreecapitatorCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Treecapitator v" + TreecapitatorPlugin.getInstance().getDescription().getVersion()).color(NamedTextColor.GREEN));
            sender.sendMessage(Component.text("Usage: /" + label + " <reload|toggle>").color(NamedTextColor.GRAY));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("treecapitator.admin")) {
                    sender.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
                    return true;
                }

                TreecapitatorPlugin.getInstance().reloadPluginConfig();
                sender.sendMessage(Component.text("Treecapitator configuration reloaded.").color(NamedTextColor.GREEN));
                return true;

            case "toggle":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Only players can toggle Treecapitator.").color(NamedTextColor.RED));
                    return true;
                }

                if (!player.hasPermission("treecapitator.use")) {
                    player.sendMessage(Component.text("You do not have permission to use Treecapitator.").color(NamedTextColor.RED));
                    return true;
                }

                UUID uuid = player.getUniqueId();
                if (BlockBreakListener.disabledPlayers.contains(uuid)) {
                    BlockBreakListener.disabledPlayers.remove(uuid);
                    player.sendMessage(Component.text("Treecapitator has been enabled for you.").color(NamedTextColor.GREEN));
                } else {
                    BlockBreakListener.disabledPlayers.add(uuid);
                    player.sendMessage(Component.text("Treecapitator has been disabled for you.").color(NamedTextColor.YELLOW));
                }
                return true;

            default:
                sender.sendMessage(Component.text("Unknown subcommand. Usage: /" + label + " <reload|toggle>").color(NamedTextColor.RED));
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("treecapitator.admin") && "reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            if (sender instanceof Player && sender.hasPermission("treecapitator.use") && "toggle".startsWith(args[0].toLowerCase())) {
                completions.add("toggle");
            }
            return completions;
        }
        return Collections.emptyList();
    }
}

package io.github.derec4.treecapitator.commands;

import io.github.derec4.treecapitator.TreecapitatorPlugin;
import io.github.derec4.treecapitator.listeners.BlockBreakListener;
import org.bukkit.ChatColor;
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
            sender.sendMessage(ChatColor.GREEN + "Treecapitator v" + TreecapitatorPlugin.getInstance().getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "Usage: /" + label + " <reload|toggle>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("treecapitator.admin")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }

                TreecapitatorPlugin.getInstance().reloadPluginConfig();
                sender.sendMessage(ChatColor.GREEN + "Treecapitator configuration reloaded.");
                return true;

            case "toggle":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can toggle Treecapitator.");
                    return true;
                }

                if (!player.hasPermission("treecapitator.use")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use Treecapitator.");
                    return true;
                }

                UUID uuid = player.getUniqueId();
                if (BlockBreakListener.disabledPlayers.contains(uuid)) {
                    BlockBreakListener.disabledPlayers.remove(uuid);
                    player.sendMessage(ChatColor.GREEN + "Treecapitator has been enabled for you.");
                } else {
                    BlockBreakListener.disabledPlayers.add(uuid);
                    player.sendMessage(ChatColor.YELLOW + "Treecapitator has been disabled for you.");
                }
                return true;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /" + label + " <reload|toggle>");
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

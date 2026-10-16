package io.github.derec4.treecapitator.commands;

import io.github.derec4.treecapitator.TreecapitatorPlugin;
import io.github.derec4.treecapitator.listeners.BlockBreakListener;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TreecapitatorCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (args.length == 0) {
            sender.sendMessage(Component.text("Treecapitator v" + TreecapitatorPlugin.getInstance().getDescription().getVersion()).color(NamedTextColor.GREEN));
            sender.sendMessage(Component.text("Usage: /treecapitator <reload|toggle>").color(NamedTextColor.GRAY));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("treecapitator.admin")) {
                    sender.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
                    return;
                }

                TreecapitatorPlugin.getInstance().reloadPluginConfig();
                sender.sendMessage(Component.text("Treecapitator configuration reloaded.").color(NamedTextColor.GREEN));
                return;

            case "toggle":
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Only players can toggle Treecapitator.").color(NamedTextColor.RED));
                    return;
                }

                if (!player.hasPermission("treecapitator.use")) {
                    player.sendMessage(Component.text("You do not have permission to use Treecapitator.").color(NamedTextColor.RED));
                    return;
                }

                UUID uuid = player.getUniqueId();
                if (BlockBreakListener.disabledPlayers.contains(uuid)) {
                    BlockBreakListener.disabledPlayers.remove(uuid);
                    player.sendMessage(Component.text("Treecapitator has been enabled for you.").color(NamedTextColor.GREEN));
                } else {
                    BlockBreakListener.disabledPlayers.add(uuid);
                    player.sendMessage(Component.text("Treecapitator has been disabled for you.").color(NamedTextColor.YELLOW));
                }
                return;

            default:
                sender.sendMessage(Component.text("Unknown subcommand. Usage: /treecapitator <reload|toggle>").color(NamedTextColor.RED));
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            String current = args.length == 0 ? "" : args[0].toLowerCase();
            CommandSender sender = source.getSender();
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("treecapitator.admin") && "reload".startsWith(current)) {
                completions.add("reload");
            }
            if (sender instanceof Player && sender.hasPermission("treecapitator.use") && "toggle".startsWith(current)) {
                completions.add("toggle");
            }
            return completions;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("treecapitator.use") || sender.hasPermission("treecapitator.admin");
    }
}

package io.github.derec4.treecapitator;

import io.github.derec4.treecapitator.commands.TreecapitatorCommand;
import io.github.derec4.treecapitator.config.TreecapitatorConfig;
import io.github.derec4.treecapitator.listeners.BlockBreakListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class TreecapitatorPlugin extends JavaPlugin {

    private static TreecapitatorPlugin instance;

    public static TreecapitatorPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        TreecapitatorConfig.load(getConfig());

        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);

        TreecapitatorCommand command = new TreecapitatorCommand();
        PluginCommand pluginCommand = getCommand("treecapitator");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        String version = getDescription().getVersion();
        console.sendMessage("");
        console.sendMessage(ChatColor.GREEN + "  |_______|                             ");
        console.sendMessage(ChatColor.GREEN + "  | Derex |     Treecapitator v" + version);
        console.sendMessage(ChatColor.GREEN + "  |_______|     Running on " + Bukkit.getName() + " - " + Bukkit.getVersion());
        console.sendMessage("");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        TreecapitatorConfig.load(getConfig());
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}

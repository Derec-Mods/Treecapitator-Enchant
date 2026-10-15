package io.github.derec4.treecapitator;

import io.github.derec4.treecapitator.config.TreecapitatorConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class TreecapitatorPlugin extends JavaPlugin {

    private static TreecapitatorPlugin instance;
    private TreecapitatorConfig pluginConfig;

    public static TreecapitatorPlugin getInstance() {
        return instance;
    }

    public TreecapitatorConfig getPluginConfig() {
        return pluginConfig;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        pluginConfig = new TreecapitatorConfig();
        pluginConfig.load(getConfig());

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        String version = getDescription().getVersion();
        console.sendMessage(Component.text(""));
        console.sendMessage(Component.text("  |_______|                             ").color(NamedTextColor.GREEN));
        console.sendMessage(Component.text("  | Derex |     Treecapitator v" + version).color(NamedTextColor.GREEN));
        console.sendMessage(Component.text("  |_______|     Running on " + Bukkit.getName() + " - " + Bukkit.getVersion()).color(NamedTextColor.GREEN));
        console.sendMessage(Component.text(""));
    }

    public void reloadPluginConfig() {
        reloadConfig();
        pluginConfig.load(getConfig());
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}

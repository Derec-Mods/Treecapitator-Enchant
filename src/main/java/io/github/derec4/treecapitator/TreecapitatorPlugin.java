package io.github.derec4.treecapitator;

import io.github.derec4.treecapitator.commands.TreecapitatorCommand;
import io.github.derec4.treecapitator.compatibility.VeinminerCompatibility;
import io.github.derec4.treecapitator.config.TreecapitatorConfig;
import io.github.derec4.treecapitator.listeners.BlockBreakListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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
        getServer().getPluginManager().registerEvents(new VeinminerCompatibility(), this);

        registerCommand("treecapitator", "Treecapitator main command", List.of("tc"), new TreecapitatorCommand());

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
        TreecapitatorConfig.load(getConfig());
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}

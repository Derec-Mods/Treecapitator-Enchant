package io.github.derec4.treecapitator.compatibility;

import io.github.derec4.treecapitator.utils.TreeUtils;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class VeinminerCompatibility implements Listener {

    private static Enchantment cachedLoggingEnchant;

    public static Enchantment getLoggingEnchantment() {
        if (cachedLoggingEnchant == null) {
            NamespacedKey key = new NamespacedKey("treecapitator", "logging");
            try {
                cachedLoggingEnchant = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(key);
            } catch (Throwable ignored) {
            }
            if (cachedLoggingEnchant == null) {
                cachedLoggingEnchant = Registry.ENCHANTMENT.get(key);
            }
        }
        return cachedLoggingEnchant;
    }

    public static void checkAndAddLogging(ItemStack item) {
        if (item == null || !TreeUtils.isAxe(item.getType()) || !item.hasItemMeta()) {
            return;
        }

        Enchantment logging = getLoggingEnchantment();
        ItemMeta meta = item.getItemMeta();
        if (logging == null || !meta.hasEnchants()) {
            return;
        }

        boolean modified = false;
        for (Enchantment enchant : meta.getEnchants().keySet()) {
            if (enchant.getKey().asString().toLowerCase().contains("veinmin")) {
                meta.removeEnchant(enchant);
                modified = true;
            }
        }

        if (modified) {
            meta.addEnchant(logging, 1, true);
            item.setItemMeta(meta);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (ItemStack item : event.getPlayer().getInventory().getContents()) {
            checkAndAddLogging(item);
        }
    }
}

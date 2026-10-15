package io.github.derec4.treecapitator.compatibility;

import io.github.derec4.treecapitator.utils.TreeUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class VeinminerCompatibility implements Listener {

    public static void checkAndAddLogging(ItemStack item) {
        if (item == null || !TreeUtils.isAxe(item.getType())) {
            return;
        }

        NamespacedKey loggingKey = new NamespacedKey("treecapitator", "logging");
        Enchantment loggingEnchant = Registry.ENCHANTMENT.get(loggingKey);
        if (loggingEnchant == null) {
            return;
        }

        List<Enchantment> toRemove = new ArrayList<>();
        for (Enchantment enchant : item.getEnchantments().keySet()) {
            if (enchant.getKey().toString().toLowerCase().contains("veinmin")) {
                toRemove.add(enchant);
            }
        }

        if (!toRemove.isEmpty()) {
            for (Enchantment enchant : toRemove) {
                item.removeEnchantment(enchant);
            }
            if (!item.containsEnchantment(loggingEnchant)) {
                item.addUnsafeEnchantment(loggingEnchant, 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (ItemStack item : event.getPlayer().getInventory().getContents()) {
            checkAndAddLogging(item);
        }
    }
}

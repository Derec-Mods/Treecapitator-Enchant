package io.github.derec4.treecapitator.listeners;

import io.github.derec4.treecapitator.config.TreecapitatorConfig;
import io.github.derec4.treecapitator.engine.TreeFeller;
import io.github.derec4.treecapitator.utils.TreeUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BlockBreakListener implements Listener {

    public static final Set<UUID> disabledPlayers = new HashSet<>();
    private final Set<Block> processingBlocks = new HashSet<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block origin = event.getBlock();
        if (processingBlocks.contains(origin)) {
            return;
        }

        if (!TreecapitatorConfig.enabled) {
            return;
        }

        if (!TreecapitatorConfig.isWorldAllowed(origin.getWorld().getName())) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        if (!player.hasPermission("treecapitator.use")) {
            return;
        }

        if (disabledPlayers.contains(player.getUniqueId())) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!TreeUtils.isAxe(item.getType())) {
            return;
        }

        if (TreecapitatorConfig.requireEnchantment) {
            NamespacedKey key = new NamespacedKey("treecapitator", "logging");
            Enchantment loggingEnchant = Registry.ENCHANTMENT.get(key);
            if (loggingEnchant == null || !item.containsEnchantment(loggingEnchant)) {
                return;
            }
        }

        if (TreecapitatorConfig.cooldownMs > 0 && !player.hasPermission("treecapitator.bypass.cooldown")) {
            long now = System.currentTimeMillis();
            Long lastChop = cooldowns.get(player.getUniqueId());
            if (lastChop != null && (now - lastChop) < TreecapitatorConfig.cooldownMs) {
                return;
            }
            cooldowns.put(player.getUniqueId(), now);
        }

        if (!TreeUtils.isLog(origin.getType(), TreecapitatorConfig.includeGiantMushrooms)) {
            return;
        }

        List<Block> logs = TreeFeller.findTreeLogs(origin);
        if (logs.isEmpty()) {
            return;
        }

        int successfulBreaks = 0;
        for (Block target : logs) {
            if (target.equals(origin)) {
                continue;
            }

            if (target.getType().isAir() || target.isLiquid() || target.getType().getHardness() < 0) {
                continue;
            }

            processingBlocks.add(target);
            try {
                BlockBreakEvent fakeEvent = new BlockBreakEvent(target, player);
                Bukkit.getPluginManager().callEvent(fakeEvent);

                if (!fakeEvent.isCancelled()) {
                    target.breakNaturally(item, true, TreecapitatorConfig.showAnimation);
                    successfulBreaks++;
                }
            } finally {
                processingBlocks.remove(target);
            }
        }

        if (TreecapitatorConfig.consumeDurability && successfulBreaks > 0) {
            for (int i = 0; i < successfulBreaks; i++) {
                if (item.getType().isAir()) {
                    break;
                }
                player.damageItemStack(item, 1);
            }
        }
    }
}

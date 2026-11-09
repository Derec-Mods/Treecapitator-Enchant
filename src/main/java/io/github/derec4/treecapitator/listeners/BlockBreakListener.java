package io.github.derec4.treecapitator.listeners;

import io.github.derec4.treecapitator.TreecapitatorPlugin;
import io.github.derec4.treecapitator.compatibility.VeinminerCompatibility;
import io.github.derec4.treecapitator.config.TreecapitatorConfig;
import io.github.derec4.treecapitator.engine.TreeFeller;
import io.github.derec4.treecapitator.utils.TreeUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class BlockBreakListener implements Listener {

    public static final Set<UUID> disabledPlayers = new HashSet<>();
    private final Set<Block> processingBlocks = new HashSet<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block origin = event.getBlock();
        if (processingBlocks.contains(origin) || !TreecapitatorConfig.enabled || !TreecapitatorConfig.isWorldAllowed(origin.getWorld().getName())) {
            return;
        }

        if (!TreeUtils.isLog(origin.getType(), TreecapitatorConfig.includeGiantMushrooms)) {
            return;
        }

        Player player = event.getPlayer();
        if ((player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) || !player.hasPermission("treecapitator.use") || disabledPlayers.contains(player.getUniqueId())) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!TreeUtils.isAxe(item.getType())) {
            return;
        }

        VeinminerCompatibility.checkAndAddLogging(item);

        Enchantment logging = VeinminerCompatibility.getLoggingEnchantment();
        if (TreecapitatorConfig.requireEnchantment && (logging == null || !item.containsEnchantment(logging))) {
            return;
        }

        if (TreecapitatorConfig.cooldownMs > 0 && !player.hasPermission("treecapitator.bypass.cooldown")) {
            long now = System.currentTimeMillis();
            Long lastChop = cooldowns.get(player.getUniqueId());
            if (lastChop != null && (now - lastChop) < TreecapitatorConfig.cooldownMs) {
                return;
            }
            cooldowns.put(player.getUniqueId(), now);
        }

        List<Block> logs = TreeFeller.findTreeLogs(origin);
        if (logs.isEmpty()) {
            return;
        }

        List<ItemStack> mergedDrops = TreecapitatorConfig.mergeDrops ? new ArrayList<>() : null;

        if (!TreecapitatorConfig.showAnimation) {
            for (Block target : logs) {
                if (!target.equals(origin)) {
                    breakBlock(target, player, item, mergedDrops);
                }
            }
            dropMerged(origin, mergedDrops);
            return;
        }

        Queue<Block> queue = new ArrayDeque<>(logs);
        queue.remove(origin);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || item.getType().isAir() || queue.isEmpty()) {
                    dropMerged(origin, mergedDrops);
                    cancel();
                    return;
                }
                for (int i = 0; i < 2 && !queue.isEmpty(); i++) {
                    breakBlock(queue.poll(), player, item, mergedDrops);
                }
            }
        }.runTaskTimer(TreecapitatorPlugin.getInstance(), 1L, 1L);
    }

    private void breakBlock(Block target, Player player, ItemStack item, List<ItemStack> mergedDrops) {
        if (target == null || target.getType().isAir() || target.isLiquid() || target.getType().getHardness() < 0) {
            return;
        }

        processingBlocks.add(target);
        try {
            BlockBreakEvent fakeEvent = new BlockBreakEvent(target, player);
            Bukkit.getPluginManager().callEvent(fakeEvent);
            if (fakeEvent.isCancelled()) {
                return;
            }

            BlockData blockData = target.getBlockData();
            if (TreecapitatorConfig.mergeDrops) {
                target.getDrops(item, player).forEach(d -> mergeDrop(mergedDrops, d));
                target.setType(Material.AIR);
            } else {
                target.breakNaturally(item, false, true);
            }

            if (TreecapitatorConfig.showAnimation) {
                SoundGroup soundGroup = blockData.getSoundGroup();
                Location center = target.getLocation().add(0.5, 0.5, 0.5);
                target.getWorld().spawnParticle(Particle.BLOCK, center, 20, 0.25, 0.25, 0.25, blockData);
                target.getWorld().playSound(center, soundGroup.getBreakSound(), SoundCategory.BLOCKS, (soundGroup.getVolume() + 1.0f) / 2.0f, soundGroup.getPitch() * 0.8f);
            }

            if (TreecapitatorConfig.consumeDurability && !item.getType().isAir()) {
                player.damageItemStack(item, 1);
            }
        } finally {
            processingBlocks.remove(target);
        }
    }

    private void dropMerged(Block origin, List<ItemStack> mergedDrops) {
        if (mergedDrops != null && !mergedDrops.isEmpty()) {
            Location dropLoc = origin.getLocation().add(0.5, 0.5, 0.5);
            for (ItemStack drop : mergedDrops) {
                origin.getWorld().dropItemNaturally(dropLoc, drop);
            }
            mergedDrops.clear();
        }
    }

    private void mergeDrop(List<ItemStack> list, ItemStack drop) {
        int remaining = drop.getAmount();
        int maxStack = drop.getMaxStackSize();
        for (ItemStack existing : list) {
            if (existing.isSimilar(drop) && existing.getAmount() < maxStack) {
                int canAdd = Math.min(remaining, maxStack - existing.getAmount());
                existing.setAmount(existing.getAmount() + canAdd);
                remaining -= canAdd;
                if (remaining <= 0) {
                    return;
                }
            }
        }
        while (remaining > 0) {
            int amount = Math.min(remaining, maxStack);
            ItemStack newStack = drop.clone();
            newStack.setAmount(amount);
            list.add(newStack);
            remaining -= amount;
        }
    }
}

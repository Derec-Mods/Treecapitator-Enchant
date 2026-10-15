package io.github.derec4.treecapitator.listeners;

import io.github.derec4.treecapitator.config.TreecapitatorConfig;
import io.github.derec4.treecapitator.engine.TreeFeller;
import io.github.derec4.treecapitator.utils.TreeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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

        if (!TreeUtils.isLog(origin.getType(), TreecapitatorConfig.includeGiantMushrooms)) {
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

        int successfulBreaks = 0;
        List<ItemStack> mergedDrops = TreecapitatorConfig.mergeDrops ? new ArrayList<>() : null;

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
                    if (TreecapitatorConfig.mergeDrops) {
                        Collection<ItemStack> drops = target.getDrops(item, player);
                        for (ItemStack drop : drops) {
                            mergeDrop(mergedDrops, drop);
                        }
                        if (TreecapitatorConfig.showAnimation) {
                            target.getWorld().playEffect(target.getLocation(), Effect.STEP_SOUND, target.getType());
                        }
                        target.setType(Material.AIR);
                    } else {
                        if (TreecapitatorConfig.showAnimation) {
                            target.getWorld().playEffect(target.getLocation(), Effect.STEP_SOUND, target.getType());
                        }
                        target.breakNaturally(item);
                    }
                    successfulBreaks++;
                }
            } finally {
                processingBlocks.remove(target);
            }
        }

        if (TreecapitatorConfig.mergeDrops && mergedDrops != null && !mergedDrops.isEmpty()) {
            Location dropLoc = origin.getLocation().add(0.5, 0.5, 0.5);
            for (ItemStack drop : mergedDrops) {
                origin.getWorld().dropItemNaturally(dropLoc, drop);
            }
        }

        if (TreecapitatorConfig.consumeDurability && successfulBreaks > 0) {
            if (item.getItemMeta() instanceof Damageable damageable) {
                int maxDurability = item.getType().getMaxDurability();
                int unbreaking = item.getEnchantmentLevel(Enchantment.UNBREAKING);
                int currentDamage = damageable.getDamage();
                for (int i = 0; i < successfulBreaks; i++) {
                    if (unbreaking > 0 && ThreadLocalRandom.current().nextInt(unbreaking + 1) != 0) {
                        continue;
                    }
                    currentDamage++;
                    if (currentDamage >= maxDurability) {
                        break;
                    }
                }
                if (currentDamage >= maxDurability) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                } else {
                    damageable.setDamage(currentDamage);
                    item.setItemMeta(damageable);
                }
            }
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

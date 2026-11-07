package io.github.derec4.treecapitator.engine;

import io.github.derec4.treecapitator.config.TreecapitatorConfig;
import io.github.derec4.treecapitator.utils.TreeUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class TreeFeller {

    private static final Map<Block, Long> naturalStumps = new HashMap<>();

    public static List<Block> findTreeLogs(Block origin) {
        Material originMaterial = origin.getType();
        boolean includeMushrooms = TreecapitatorConfig.includeGiantMushrooms;
        boolean antiGrief = TreecapitatorConfig.antiGrief;

        if (!TreeUtils.isLog(originMaterial, includeMushrooms) || (antiGrief && !TreeUtils.isAntiGriefValid(origin, includeMushrooms))) {
            return Collections.emptyList();
        }

        boolean fellEntireTree = TreecapitatorConfig.fellEntireTree;
        int maxLogs = TreecapitatorConfig.maxLogsPerChop;
        int maxSearch = fellEntireTree ? 512 : Math.max(maxLogs * 4, 256);

        long now = System.currentTimeMillis();
        naturalStumps.entrySet().removeIf(entry -> now - entry.getValue() > 300000L);

        Set<Block> visited = new HashSet<>();
        List<Block> logs = new ArrayList<>();
        Queue<Block> queue = new ArrayDeque<>();

        visited.add(origin);
        queue.add(origin);

        boolean matchLogs = TreecapitatorConfig.matchLogs;
        boolean hasNaturalLeaves = !antiGrief || TreeUtils.touchesMatchingLeaf(origin) || naturalStumps.containsKey(origin);

        while (!queue.isEmpty()) {
            Block current = queue.poll();
            logs.add(current);

            if (logs.size() >= maxSearch) {
                break;
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }

                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (!visited.add(neighbor)) {
                            continue;
                        }

                        Material neighborType = neighbor.getType();
                        if (!TreeUtils.isLog(neighborType, includeMushrooms) || !TreeUtils.matchesLog(originMaterial, neighborType, matchLogs)) {
                            continue;
                        }

                        if (antiGrief && !TreeUtils.isAntiGriefValid(neighbor, includeMushrooms)) {
                            continue;
                        }

                        if (antiGrief && !hasNaturalLeaves && (TreeUtils.touchesMatchingLeaf(neighbor) || naturalStumps.containsKey(neighbor))) {
                            hasNaturalLeaves = true;
                        }

                        queue.add(neighbor);
                    }
                }
            }
        }

        if (antiGrief && !hasNaturalLeaves) {
            return Collections.emptyList();
        }

        logs.sort(Comparator.comparingDouble(b -> b.getLocation().distanceSquared(origin.getLocation())));
        if (!fellEntireTree && maxLogs > 0 && logs.size() > maxLogs) {
            for (int i = maxLogs; i < logs.size(); i++) {
                naturalStumps.put(logs.get(i), now);
            }
            List<Block> toFell = new ArrayList<>(logs.subList(0, maxLogs));
            toFell.forEach(naturalStumps::remove);
            return toFell;
        }

        logs.forEach(naturalStumps::remove);
        return logs;
    }
}

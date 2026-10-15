package io.github.derec4.treecapitator.engine;

import io.github.derec4.treecapitator.config.TreecapitatorConfig;
import io.github.derec4.treecapitator.utils.TreeUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class TreeFeller {

    public static List<Block> findTreeLogs(Block origin) {
        Material originMaterial = origin.getType();
        boolean includeMushrooms = TreecapitatorConfig.includeGiantMushrooms;
        boolean matchLogs = TreecapitatorConfig.matchLogs;
        boolean antiGrief = TreecapitatorConfig.antiGrief;
        int maxLogs = TreecapitatorConfig.maxLogsPerChop;

        if (!TreeUtils.isLog(originMaterial, includeMushrooms)) {
            return Collections.emptyList();
        }

        if (antiGrief && !TreeUtils.isAntiGriefValid(origin, includeMushrooms)) {
            return Collections.emptyList();
        }

        Set<Block> visited = new HashSet<>();
        List<Block> logs = new ArrayList<>();
        Queue<Block> queue = new ArrayDeque<>();

        visited.add(origin);
        queue.add(origin);

        while (!queue.isEmpty()) {
            Block current = queue.poll();
            logs.add(current);

            if (logs.size() > maxLogs) {
                return Collections.emptyList();
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
                        if (!TreeUtils.isLog(neighborType, includeMushrooms)) {
                            continue;
                        }

                        if (!TreeUtils.matchesLog(originMaterial, neighborType, matchLogs)) {
                            continue;
                        }

                        if (antiGrief && !TreeUtils.isAntiGriefValid(neighbor, includeMushrooms)) {
                            continue;
                        }

                        queue.add(neighbor);
                    }
                }
            }
        }

        logs.sort((b1, b2) -> Integer.compare(b2.getY(), b1.getY()));
        return logs;
    }
}

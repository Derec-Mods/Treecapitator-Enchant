package io.github.derec4.treecapitator.utils;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;

public class TreeUtils {

    private static final BlockFace[] ORTHOGONAL_FACES = {
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST
    };

    public static boolean isAxe(Material material) {
        if (material == null) {
            return false;
        }
        return material.name().endsWith("_AXE");
    }

    public static boolean isLog(Material material, boolean includeMushrooms) {
        if (material == null) {
            return false;
        }
        if (Tag.LOGS.isTagged(material) || material.name().endsWith("_LOG") || material.name().endsWith("_STEM")
                || material.name().endsWith("_WOOD") || material.name().endsWith("_HYPHAE")) {
            return true;
        }
        return includeMushrooms && material == Material.MUSHROOM_STEM;
    }

    public static boolean matchesLog(Material base, Material target, boolean matchLogs) {
        if (!matchLogs) {
            return true;
        }
        return base == target;
    }

    public static boolean isAntiGriefValid(Block block, boolean includeMushrooms) {
        Material material = block.getType();
        if (!isLog(material, includeMushrooms)) {
            return false;
        }

        BlockData data = block.getBlockData();
        if (data instanceof Orientable orientable) {
            if (orientable.getAxis() == Axis.Y) {
                return true;
            }
        }

        if (material == Material.CHERRY_LOG || material == Material.STRIPPED_CHERRY_LOG
                || material == Material.CHERRY_WOOD || material == Material.STRIPPED_CHERRY_WOOD) {
            int orthogonalLogs = 0;
            for (BlockFace face : ORTHOGONAL_FACES) {
                if (isLog(block.getRelative(face).getType(), includeMushrooms)) {
                    orthogonalLogs++;
                }
            }
            if (orthogonalLogs <= 2) {
                return true;
            }
        }

        if (material == Material.MUSHROOM_STEM) {
            return true;
        }

        return false;
    }
}

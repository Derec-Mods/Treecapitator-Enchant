package io.github.derec4.treecapitator.utils;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Leaves;

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

    public static boolean isMatchingLeaf(Material log, Block leafBlock) {
        if (leafBlock == null) {
            return false;
        }

        BlockData data = leafBlock.getBlockData();
        if (data instanceof Leaves leaves) {
            if (leaves.isPersistent()) {
                return false;
            }
        } else {
            Material leafType = leafBlock.getType();
            if (log == Material.CRIMSON_STEM || log == Material.STRIPPED_CRIMSON_STEM || log == Material.CRIMSON_HYPHAE || log == Material.STRIPPED_CRIMSON_HYPHAE) {
                return leafType == Material.NETHER_WART_BLOCK || leafType == Material.SHROOMLIGHT;
            }
            if (log == Material.WARPED_STEM || log == Material.STRIPPED_WARPED_STEM || log == Material.WARPED_HYPHAE || log == Material.STRIPPED_WARPED_HYPHAE) {
                return leafType == Material.WARPED_WART_BLOCK || leafType == Material.SHROOMLIGHT;
            }
            if (log == Material.MUSHROOM_STEM) {
                return leafType == Material.RED_MUSHROOM_BLOCK || leafType == Material.BROWN_MUSHROOM_BLOCK;
            }
            return false;
        }

        Material leafType = leafBlock.getType();
        String logName = log.name().replace("STRIPPED_", "").replace("_LOG", "").replace("_WOOD", "");
        String leafName = leafType.name().replace("_LEAVES", "");

        if (logName.equals(leafName)) {
            return true;
        }

        if (logName.equals("OAK") && (leafType == Material.AZALEA_LEAVES || leafType == Material.FLOWERING_AZALEA_LEAVES)) {
            return true;
        }

        return false;
    }

    public static boolean touchesMatchingLeaf(Block logBlock) {
        Material logMaterial = logBlock.getType();
        for (BlockFace face : ORTHOGONAL_FACES) {
            if (isMatchingLeaf(logMaterial, logBlock.getRelative(face))) {
                return true;
            }
        }
        return false;
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

        if (touchesMatchingLeaf(block)) {
            return true;
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

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
        return includeMushrooms && material.name().equals("MUSHROOM_STEM");
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
            String leafName = leafBlock.getType().name();
            String logName = log.name();
            if (logName.contains("CRIMSON")) {
                return leafName.equals("NETHER_WART_BLOCK") || leafName.equals("SHROOMLIGHT");
            }
            if (logName.contains("WARPED")) {
                return leafName.equals("WARPED_WART_BLOCK") || leafName.equals("SHROOMLIGHT");
            }
            if (logName.equals("MUSHROOM_STEM")) {
                return leafName.equals("RED_MUSHROOM_BLOCK") || leafName.equals("BROWN_MUSHROOM_BLOCK");
            }
            return false;
        }

        String logName = log.name().replace("STRIPPED_", "").replace("_LOG", "").replace("_WOOD", "");
        String leafName = leafBlock.getType().name().replace("_LEAVES", "");

        if (logName.equals(leafName)) {
            return true;
        }

        if (logName.equals("OAK") && leafName.contains("AZALEA")) {
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

        if (material.name().contains("CHERRY")) {
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

        if (material.name().equals("MUSHROOM_STEM")) {
            return true;
        }

        return false;
    }
}

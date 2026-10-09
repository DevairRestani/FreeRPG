package mc.carlton.freerpg.serverInfo;

import mc.carlton.freerpg.FreeRPG;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlacedBlocksManager {
    //The set of all tracked (player-placed) blocks on the server.
    //Concurrent so the async periodic save can copy it while the main thread adds/removes blocks.
    //(The old HashSet + "copy in progress" flag could throw ConcurrentModificationException, leave the flag stuck,
    // and then re-schedule every add/remove each tick forever while new placements were never saved.)
    private static Set<Location> blocks = ConcurrentHashMap.newKeySet();


    public boolean isBlockTracked(Block block) {
        Location location = block.getLocation();
        return isLocationTracked(location);
    }
    public boolean isLocationTracked(Location location) {
        return blocks.contains(location);
    }

    public HashSet<Location> getBlocks() {
        return new HashSet<>(blocks);
    }

    public void setBlocksMap(HashSet<Location> newblocks) {
        Set<Location> newSet = ConcurrentHashMap.newKeySet();
        newSet.addAll(newblocks);
        blocks = newSet;
    }

    public void addBlock(Block block) {
        Location location = block.getLocation();
        addLocation(location);
    }

    public void addLocation(Location location) {
        blocks.add(location);
    }
    public void removeBlock(Block block) {
        Location location = block.getLocation();
        removeLocation(location);
    }
    public void removeLocation(Location location) {
        blocks.remove(location);
    }

}

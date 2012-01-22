package me.exphc.SilkSpawners;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Formatter;
import java.lang.Byte;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.Material.*;
import org.bukkit.material.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import org.bukkit.inventory.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.scheduler.*;
import org.bukkit.*;

import org.bukkit.craftbukkit.block.CraftCreatureSpawner;

class SilkSpawnersBlockListener extends BlockListener {
    static Logger log = Logger.getLogger("Minecraft");

    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() != Material.MOB_SPAWNER) {
            return;
        }

        Player player = event.getPlayer();
        CraftCreatureSpawner spawner = new CraftCreatureSpawner(block);

        log.info("broke spawner for "+spawner.getCreatureType().getName());
        // TODO: set data on item
    }

    public void onBlockPlace(BlockPlaceEvent event) {
        Block blockPlaced = event.getBlockPlaced();

        if (blockPlaced.getType() != Material.MOB_SPAWNER) {
            return;
        }

        log.info("place spawner ");

        // TODO: get data from item

        CraftCreatureSpawner spawner = new CraftCreatureSpawner(blockPlaced);
        spawner.setCreatureType(CreatureType.fromName("Zombie"));   
    }
}

public class SilkSpawners extends JavaPlugin {
    static Logger log = Logger.getLogger("Minecraft");
    SilkSpawnersBlockListener blockListener;

    public void onEnable() {

        blockListener = new SilkSpawnersBlockListener();

        Bukkit.getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, org.bukkit.event.Event.Priority.Normal, this);
        Bukkit.getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, blockListener, org.bukkit.event.Event.Priority.Normal, this);


        log.info("SilkSpawners enabled");
    }

    public void onDisable() {
        log.info("SilkSpawners disabled");
    }
}



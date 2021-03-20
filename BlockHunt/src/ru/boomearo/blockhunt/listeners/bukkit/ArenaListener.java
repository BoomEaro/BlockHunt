package ru.boomearo.blockhunt.listeners.bukkit;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.world.StructureGrowEvent;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.objects.BHArena;

public class ArenaListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Location loc = e.getBlock().getLocation();
        
        BHArena arena = BlockHunt.getInstance().getBlockHuntManager().getArenaByLocation(loc);
        if (arena != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent e) {
        if (e.isCancelled()) {
            return;
        }
        
        Location loc = e.getLocation();
        
        BHArena arena = BlockHunt.getInstance().getBlockHuntManager().getArenaByLocation(loc);
        if (arena != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockGrowEvent(BlockGrowEvent e) {
        if (e.isCancelled()) {
            return;
        }
        
        Block b = e.getBlock();
        if (b == null) {
            return;
        }
        
        Location loc = b.getLocation();
        
        BHArena arena = BlockHunt.getInstance().getBlockHuntManager().getArenaByLocation(loc);
        if (arena != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockBurnEvent(BlockBurnEvent e) {
        if (e.isCancelled()) {
            return;
        }
        
        Block b = e.getBlock();
        if (b == null) {
            return;
        }
        
        Location loc = b.getLocation();
        
        BHArena arena = BlockHunt.getInstance().getBlockHuntManager().getArenaByLocation(loc);
        if (arena != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockFadeEvent(BlockFadeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        
        Block b = e.getBlock();
        if (b == null) {
            return;
        }
        
        Location loc = b.getLocation();
        
        BHArena arena = BlockHunt.getInstance().getBlockHuntManager().getArenaByLocation(loc);
        if (arena != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockFormEvent(BlockFormEvent e) {
        if (e.isCancelled()) {
            return;
        }
        
        Block b = e.getBlock();
        if (b == null) {
            return;
        }
        
        Location loc = b.getLocation();
        
        BHArena arena = BlockHunt.getInstance().getBlockHuntManager().getArenaByLocation(loc);
        if (arena != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent e) {
        if (e.isCancelled()) {
            return;
        }
        
        Block b = e.getBlock();
        if (b == null) {
            return;
        }
        
        Location loc = b.getLocation();
        
        BHArena arena = BlockHunt.getInstance().getBlockHuntManager().getArenaByLocation(loc);
        if (arena != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockSpreadEvent(BlockSpreadEvent e) {
        if (e.isCancelled()) {
            return;
        }
        
        Block b = e.getBlock();
        if (b == null) {
            return;
        }
        
        Location loc = b.getLocation();
        
        BHArena arena = BlockHunt.getInstance().getBlockHuntManager().getArenaByLocation(loc);
        if (arena != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onStructureGrowEvent(StructureGrowEvent e) {
        if (e.isCancelled()) {
            return;
        }
        
        Location loc = e.getLocation();
        
        BHArena arena = BlockHunt.getInstance().getBlockHuntManager().getArenaByLocation(loc);
        if (arena != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onHangingBreakEvent(HangingBreakEvent e) {
        Location loc = e.getEntity().getLocation();
        
        BHArena arena = BlockHunt.getInstance().getBlockHuntManager().getArenaByLocation(loc);
        if (arena != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent e) {
        if (e.isCancelled()) {
            return;
        }

        handleExplode(e.blockList(), e);
    }

    @EventHandler
    public void onBlockExplodeEvent(BlockExplodeEvent e) {
        if (e.isCancelled()) {
            return;
        }

        handleExplode(e.blockList(), e);
    }
    
    private static void handleExplode(List<Block> bs, Cancellable e) {

        for (Block b : bs) {
            BHArena arena = BlockHunt.getInstance().getBlockHuntManager().getArenaByLocation(b.getLocation());
            if (arena != null) {
                e.setCancelled(true);
                return;
            }
        }

    }
}
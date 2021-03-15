package ru.boomearo.blockhunt.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.state.RunningState;
import ru.boomearo.gamecontrol.objects.states.IGameState;

public class PlayerListener implements Listener {
    
    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e) {
        Player pl = e.getEntity();
        
        BHPlayer tp = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
        if (tp != null) {
            IGameState state = tp.getArena().getState();
            if (state instanceof RunningState) {
                RunningState rs = (RunningState) state;
                rs.handleDeath(tp);
            }
            e.setDroppedExp(0);
            e.getDrops().clear();
        }
    }
    
    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent e) {
        Player pl = e.getPlayer();
        
        BHPlayer tp = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
        if (tp != null) {
            Location loc = null;
            IGameState state = tp.getArena().getState();
            if (state instanceof RunningState) {
                loc = tp.getArena().getSeekersLocation();
            }
            else {
                loc = tp.getArena().getLobbyLocation();
            }
            
            if (loc != null) {
                e.setRespawnLocation(loc);
            }
            
            tp.getPlayerType().preparePlayer(tp);
        }
    }
    
    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player pl = e.getPlayer();
        
        String msg = e.getMessage();
        if (msg.equalsIgnoreCase("/blockhunt leave") || msg.equalsIgnoreCase("/bh leave")) {
            return;
        }
        
        BHPlayer tp = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
        if (tp != null) {
            e.setCancelled(true);
            pl.sendMessage(BlockHuntManager.prefix + "Вы не можете использовать эти команды в игре!");
        }
    }
    
    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Entity en = e.getEntity();
        if (en instanceof Player) {
            Player pl = (Player) en;
            
            BHPlayer tp = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
            if (tp != null) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if (e.isCancelled()) {
            return;
        }
        
        Player pl = e.getPlayer();
        BHPlayer tp = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
        if (tp != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerItemDamageEvent(PlayerItemDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player pl = e.getPlayer();
        BHPlayer tp = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
        if (tp != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player pl = e.getPlayer();
        BHPlayer tp = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
        if (tp != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL) {
            return;
        }
        
        Player pl = e.getPlayer();
        BHPlayer tp = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
        if (tp != null) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Entity en = e.getEntity();
        if (en instanceof Player) {
            Player pl = (Player) en;
            BHPlayer tp = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
            if (tp != null) {
                e.setCancelled(true);
            }
        }
    }
}

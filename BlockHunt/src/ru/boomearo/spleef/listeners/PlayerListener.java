package ru.boomearo.spleef.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.SpleefTeam;
import ru.boomearo.spleef.objects.playertype.LosePlayer;
import ru.boomearo.spleef.objects.state.RunningState;
import ru.boomearo.spleef.objects.state.RunningState.BlockOwner;

public class PlayerListener implements Listener {
    
    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e) {
        Player pl = e.getEntity();
        
        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
        if (tp != null) {
            LosePlayer lp = new LosePlayer();
            tp.setPlayerType(lp);
            
            e.setDroppedExp(0);
            e.getDrops().clear();
        }
    }
    
    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent e) {
        Player pl = e.getPlayer();
        
        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
        if (tp != null) {
            SpleefTeam team = tp.getTeam();
            Location loc = team.getSpawnPoint();
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
        if (msg.equalsIgnoreCase("/spleef leave")) {
            return;
        }
        
        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
        if (tp != null) {
            e.setCancelled(true);
            pl.sendMessage(SpleefManager.prefix + "Вы не можете использовать эти команды в игре!");
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
            
            SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
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
        
        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
        if (tp != null) {
            
            //Если игрок ломает в арене этот блок то позволяем
            //И если на арене идет игра то делаем
            Block b = e.getBlock();
            if (b.getType() == Material.SNOW_BLOCK) {
                
                IGameState state = tp.getArena().getState();
                if (state instanceof RunningState) {
                    RunningState rs = (RunningState) state;
                    
                    BlockOwner bo = rs.getBlockByLocation(b.getLocation());
                    if (bo == null) {
                        rs.addBlock(b, pl.getName());
                    }
                    
                    e.setExpToDrop(0);
                    e.setDropItems(false);
                    
                    return;
                }
            }
            
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerItemDamageEvent(PlayerItemDamageEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player pl = e.getPlayer();
        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
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
        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
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
        SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
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
            SpleefPlayer tp = Spleef.getInstance().getSpleefManager().getGamePlayer(pl.getName());
            if (tp != null) {
                e.setCancelled(true);
            }
        }
    }
}

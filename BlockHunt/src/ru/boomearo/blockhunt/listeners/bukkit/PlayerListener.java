package ru.boomearo.blockhunt.listeners.bukkit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.SolidPlayer;
import ru.boomearo.blockhunt.objects.playertype.HiderPlayer;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
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
        if (e.getCause() == DamageCause.ENTITY_ATTACK) {
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
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
        
        Player damager = null;

        if (e.getDamager() instanceof Player) {
            damager = (Player) e.getDamager();
        }
        else if (e.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) e.getDamager();

            if (proj.getShooter() instanceof Player) {
                damager = (Player) proj.getShooter();
            }
        }

        if (damager == null) {
            return;
        }
        
        if (damager == e.getEntity()) {
            return;
        }
        
        BlockHuntManager manager = BlockHunt.getInstance().getBlockHuntManager();
        BHPlayer bhDamager = manager.getGamePlayer(damager.getName());
        if (bhDamager == null) {
            return;
        }
        
        Entity entity = e.getEntity();
        if (entity.isDead()) {
            return;
        }
        
        if (!(entity instanceof Player)) {
            return;
        }
        
        Player player = (Player) entity;
        
        BHPlayer bhPlayer = manager.getGamePlayer(player.getName());
        if (bhPlayer == null) {
            return;
        }
        
        BHArena arena = bhPlayer.getArena();
        IGameState state = arena.getState();
        //В любом случае отменяем ивент
        if (!(state instanceof RunningState)) {
            e.setCancelled(true);
            return;
        }
        
        //Если дамагает атакует союзника то отменяем ивент
        if (bhDamager.getPlayerType().getClass() == bhPlayer.getPlayerType().getClass()) {
            e.setCancelled(true);
            return;
        }
        
        RunningState rs = (RunningState) state;
        
        //Когда сущность точно умрет
        double newHealth = player.getHealth() - e.getFinalDamage();
        if (newHealth <= 0) {
            
            rs.handleDeath(bhPlayer);
            
            e.setCancelled(true);
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
        Player pl = e.getPlayer();
        BHPlayer tp = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
        if (tp == null) {
            return;
        }
        
        e.setCancelled(true);
        
        Action a = e.getAction();
        
        if (a != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        
        Block b = e.getClickedBlock();
        if (b == null) {
            return;
        }
        
        BHArena arena = tp.getArena();
        
        SolidPlayer sb = arena.getSolidPlayerByLocation(b.getLocation());
        if (sb == null) {
            return;
        }
        
        //Игнорим если хайдеры не тыкали друг друга
        if (sb.getPlayer().getPlayerType().getClass() == tp.getPlayerType().getClass()) {
            return;
        }
        
        IPlayerType type = sb.getPlayer().getPlayerType();
        if (!(type instanceof HiderPlayer)) {
            return;
        }
        
        HiderPlayer hp = (HiderPlayer) type;

        arena.unmakeSolid(sb.getPlayer(), hp);
        
        pl.getWorld().playSound(sb.getLocation(), Sound.ENTITY_PLAYER_HURT, 999, 1);
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

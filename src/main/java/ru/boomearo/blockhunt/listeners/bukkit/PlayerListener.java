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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
            if (state instanceof RunningState rs) {
                rs.handleDeath(tp, null);
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
        if (msg.equalsIgnoreCase("/blockhunt leave") || msg.equalsIgnoreCase("/bh leave") || msg.equalsIgnoreCase("/lobby") || msg.equalsIgnoreCase("/spawn")) {
            return;
        }

        BHPlayer tp = BlockHunt.getInstance().getBlockHuntManager().getGamePlayer(pl.getName());
        if (tp != null) {
            e.setCancelled(true);
            pl.sendMessage(BlockHuntManager.prefix + "Вы не можете использовать эти команды в игре!");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        Player damager = null;

        if (e.getDamager() instanceof Player) {
            damager = (Player) e.getDamager();
        }
        else if (e.getDamager() instanceof Projectile proj) {

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

        if (!(entity instanceof Player player)) {
            return;
        }

        BHPlayer bhPlayer = manager.getGamePlayer(player.getName());
        if (bhPlayer == null) {
            return;
        }

        BHArena arena = bhPlayer.getArena();
        IGameState state = arena.getState();
        //В любом случае отменяем ивент
        if (!(state instanceof RunningState rs)) {
            e.setCancelled(true);
            return;
        }

        //Если дамагает атакует союзника то отменяем ивент
        if (bhDamager.getPlayerType().getClass() == bhPlayer.getPlayerType().getClass()) {
            e.setCancelled(true);
            return;
        }

        //Если тот кто атакует является хайдером и наносит кому то урон то снимаем с него маскировку.
        IPlayerType typeDamager = bhDamager.getPlayerType();
        if (typeDamager instanceof HiderPlayer hp) {
            hp.resetBlockCount();

            bhPlayer.getArena().unmakeSolid(bhDamager, hp);

        }


        //Если тот кто получает урон является хайдером то сбрасываем с него все
        IPlayerType typePlayer = bhPlayer.getPlayerType();
        if (typePlayer instanceof HiderPlayer hp) {
            hp.resetBlockCount();

            bhPlayer.getArena().unmakeSolid(bhPlayer, hp);

        }

        //Когда сущность точно умрет
        double newHealth = player.getHealth() - e.getFinalDamage();
        if (newHealth <= 0) {

            rs.handleDeath(bhPlayer, bhDamager);

            //Наносим урон но нулевой
            e.setDamage(0);
        }

        e.setCancelled(false);
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

        arena.unmakeSolid(sb.getPlayer(), sb.getHiderPlayer());

        pl.getWorld().playSound(sb.getLocation(), Sound.ENTITY_PLAYER_HURT, 999, 1);
    }

}

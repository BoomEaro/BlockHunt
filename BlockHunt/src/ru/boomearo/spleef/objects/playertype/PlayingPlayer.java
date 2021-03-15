package ru.boomearo.spleef.objects.playertype;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.objects.ItemButton;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.SpleefTeam;
import ru.boomearo.spleef.utils.ExpFix;

public class PlayingPlayer implements IPlayerType {
    
    private String killer;
    
    @Override
    public void preparePlayer(SpleefPlayer player) {
        if (Bukkit.isPrimaryThread()) {
            task(player);
        }
        else {
            Bukkit.getScheduler().runTask(Spleef.getInstance(), () -> {
                task(player);
            });
        }
    }
    
    public String getKiller() {
        return this.killer;
    }
    
    public void setKiller(String killer) {
        this.killer = killer;
    }
    
    private void task(SpleefPlayer player) {
        Player pl = player.getPlayer();
        
        pl.setFoodLevel(20);
        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        
        pl.setGameMode(GameMode.SURVIVAL);
        pl.setFlying(false);
        pl.setAllowFlight(false);
        
        ExpFix.setTotalExperience(player.getPlayer(), 0);
        
        PlayerInventory inv = pl.getInventory();
        inv.clear();
        
        for (ItemButton ib : ItemButton.values()) {
            inv.setItem(ib.getSlot(), ib.getItem());
        }

        inv.setHeldItemSlot(0);
        
        SpleefTeam team = player.getTeam();
        Location loc = team.getSpawnPoint();
        if (loc != null) {
            GameControl.getInstance().asyncTeleport(pl, loc);
        }
    }
    
}
package ru.boomearo.blockhunt.objects.playertype;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.ItemButton;
import ru.boomearo.blockhunt.utils.ExpFix;
import ru.boomearo.gamecontrol.GameControl;

public class HiderPlayer implements IPlayerType {

    @Override
    public void preparePlayer(BHPlayer player) {
        if (Bukkit.isPrimaryThread()) {
            task(player);
        }
        else {
            Bukkit.getScheduler().runTask(BlockHunt.getInstance(), () -> {
                task(player);
            });
        }
    }
    
    private void task(BHPlayer player) {
        Player pl = player.getPlayer();
        
        pl.setFoodLevel(20);
        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        
        pl.setGameMode(GameMode.SURVIVAL);
        pl.setFlying(false);
        pl.setAllowFlight(false);
        
        ExpFix.setTotalExperience(player.getPlayer(), 0);
        
        PlayerInventory inv = pl.getInventory();
        inv.clear();
        
        ItemButton leave = ItemButton.Leave;
        inv.setItem(leave.getSlot(), leave.getItem());
        
        Location loc = player.getArena().getHidersLocation();
        if (loc != null) {
            GameControl.getInstance().asyncTeleport(pl, loc);
        }
    }
    
}

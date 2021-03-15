package ru.boomearo.spleef.objects.playertype;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.SpleefTeam;
import ru.boomearo.spleef.utils.ExpFix;

public class SpectatingPlayer implements IPlayerType {

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
    
    private void task(SpleefPlayer player) {
        Player pl = player.getPlayer();
        
        pl.setFoodLevel(20);
        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        
        pl.setGameMode(GameMode.SPECTATOR);
        
        ExpFix.setTotalExperience(player.getPlayer(), 0);
        
        pl.getInventory().clear();
        
        SpleefTeam team = player.getTeam();
        Location loc = team.getSpawnPoint();
        if (loc != null) {
            GameControl.getInstance().asyncTeleport(pl, loc);
        }
    }
}
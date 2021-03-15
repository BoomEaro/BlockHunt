package ru.boomearo.spleef.runnable;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.objects.SpleefArena;

public class ArenasRunnable extends BukkitRunnable {
    
    public ArenasRunnable() {
        runnable();
    }
    
    private void runnable() {
        this.runTaskTimer(Spleef.getInstance(), 1, 1);
    }
    
    @Override
    public void run() {
        for (SpleefArena arena : Spleef.getInstance().getSpleefManager().getAllArenas()) {
            
            IGameState state = arena.getState();
            
            state.autoUpdateHandler();
        }
    }
}

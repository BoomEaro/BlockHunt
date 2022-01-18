package ru.boomearo.blockhunt.runnable;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.gamecontrol.objects.states.IGameState;

public class ArenasRunnable extends BukkitRunnable {
    
    public ArenasRunnable() {
        runnable();
    }
    
    private void runnable() {
        this.runTaskTimer(BlockHunt.getInstance(), 1, 1);
    }
    
    @Override
    public void run() {
        for (BHArena arena : BlockHunt.getInstance().getBlockHuntManager().getAllArenas()) {
            
            IGameState state = arena.getState();
            
            state.autoUpdateHandler();
        }
    }
}

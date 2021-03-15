package ru.boomearo.blockhunt.objects.state;

import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.objects.states.IGameState;

public class RegenState implements IGameState, SpectatorFirst {
    
    private final BHArena arena;
    
    public RegenState(BHArena arena) {
        this.arena = arena;
    }
    
    @Override
    public String getName() {
        return "§6Регенерация арены";
    }
    
    @Override
    public BHArena getArena() {
        return this.arena;
    }
    
    @Override
    public void initState() {
        this.arena.sendMessages(BlockHuntManager.prefix + "Начинаем регенерацию арены..");
        
        //Добавляем регенерацию в очередь.
        GameControl.getInstance().getGameManager().queueRegenArena(this.arena);
    }
    
    
    @Override
    public void autoUpdateHandler() {
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            if (!this.arena.getArenaRegion().isInRegion(tp.getPlayer().getLocation())) {
                tp.getPlayerType().preparePlayer(tp);
            }
        }
    }

}

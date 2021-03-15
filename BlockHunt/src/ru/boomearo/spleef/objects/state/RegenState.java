package ru.boomearo.spleef.objects.state;

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefArena;
import ru.boomearo.spleef.objects.SpleefPlayer;

public class RegenState implements IGameState, SpectatorFirst {
    
    private final SpleefArena arena;
    
    public RegenState(SpleefArena arena) {
        this.arena = arena;
    }
    
    @Override
    public String getName() {
        return "§6Регенерация арены";
    }
    
    @Override
    public SpleefArena getArena() {
        return this.arena;
    }
    
    @Override
    public void initState() {
        this.arena.sendMessages(SpleefManager.prefix + "Начинаем регенерацию арены..");
        
        //Добавляем регенерацию в очередь.
        GameControl.getInstance().getGameManager().queueRegenArena(this.arena);
    }
    
    
    @Override
    public void autoUpdateHandler() {
        for (SpleefPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            if (!this.arena.getArenaRegion().isInRegion(tp.getPlayer().getLocation())) {
                tp.getPlayerType().preparePlayer(tp);
            }
        }
    }

}

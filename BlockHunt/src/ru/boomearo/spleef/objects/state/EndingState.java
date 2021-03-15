package ru.boomearo.spleef.objects.state;

import ru.boomearo.gamecontrol.objects.states.ICountable;
import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.gamecontrol.utils.DateUtil;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefArena;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.playertype.LosePlayer;
import ru.boomearo.spleef.objects.playertype.PlayingPlayer;

public class EndingState implements IGameState, ICountable {

    private final SpleefArena arena;
    
    private int count = 5;
    
    private int cd = 20;
    
    public EndingState(SpleefArena arena) {
        this.arena = arena;
    }
    
    @Override
    public String getName() {
        return "§cКонец игры";
    }
    
    @Override
    public SpleefArena getArena() {
        return this.arena;
    }
    
    @Override
    public void initState() {
        this.arena.sendMessages(SpleefManager.prefix + "Игра закончена!");
        
        for (SpleefPlayer tp : this.arena.getAllPlayers()) {
            if (tp.getPlayerType() instanceof PlayingPlayer) {
                tp.setPlayerType(new LosePlayer());
            }
            tp.getPlayerType().preparePlayer(tp);
        }
    }
    
    @Override
    public void autoUpdateHandler() {
        for (SpleefPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            if (!this.arena.getArenaRegion().isInRegion(tp.getPlayer().getLocation())) {
                tp.getPlayerType().preparePlayer(tp);
            }
        }
        
        handleCount(this.arena);
    }
    
    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }
    
    private void handleCount(SpleefArena arena) {
        if (this.cd <= 0) {
            this.cd = 20;
            
            if (this.count <= 0) {
                arena.setState(new RegenState(arena));
                return;
            }
            
            arena.sendMessages(SpleefManager.prefix + "Следующая игра начнется через §b" + DateUtil.formatedTime(this.count, false));
            
            this.count--;
            
            return;
        }
        this.cd--;
    }


}

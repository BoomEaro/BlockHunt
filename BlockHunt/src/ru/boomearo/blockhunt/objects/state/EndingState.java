package ru.boomearo.blockhunt.objects.state;

import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.playertype.LosePlayer;
import ru.boomearo.blockhunt.objects.playertype.PlayingPlayer;
import ru.boomearo.gamecontrol.objects.states.ICountable;
import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.gamecontrol.utils.DateUtil;

public class EndingState implements IGameState, ICountable {

    private final BHArena arena;
    
    private int count = 5;
    
    private int cd = 20;
    
    public EndingState(BHArena arena) {
        this.arena = arena;
    }
    
    @Override
    public String getName() {
        return "§cКонец игры";
    }
    
    @Override
    public BHArena getArena() {
        return this.arena;
    }
    
    @Override
    public void initState() {
        this.arena.sendMessages(BlockHuntManager.prefix + "Игра закончена!");
        
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            if (tp.getPlayerType() instanceof PlayingPlayer) {
                tp.setPlayerType(new LosePlayer());
            }
            tp.getPlayerType().preparePlayer(tp);
        }
    }
    
    @Override
    public void autoUpdateHandler() {
        for (BHPlayer tp : this.arena.getAllPlayers()) {
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
    
    private void handleCount(BHArena arena) {
        if (this.cd <= 0) {
            this.cd = 20;
            
            if (this.count <= 0) {
                arena.setState(new RegenState(arena));
                return;
            }
            
            arena.sendMessages(BlockHuntManager.prefix + "Следующая игра начнется через §b" + DateUtil.formatedTime(this.count, false));
            
            this.count--;
            
            return;
        }
        this.cd--;
    }


}

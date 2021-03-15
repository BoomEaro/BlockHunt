package ru.boomearo.blockhunt.objects.state;

import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.playertype.LosePlayer;
import ru.boomearo.blockhunt.objects.playertype.PlayingPlayer;
import ru.boomearo.gamecontrol.objects.states.IWaitingState;

public class WaitingState implements IWaitingState {

    private final BHArena arena;
    
    public WaitingState(BHArena arena) {
        this.arena = arena;
    }
    
    @Override
    public String getName() {
        return "§6Ожидание игроков";
    }
    
    @Override
    public BHArena getArena() {
        return this.arena;
    }
    
    @Override 
    public void initState() {
        this.arena.sendMessages(BlockHuntManager.prefix + "Ожидание игроков..");
        
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            //Возвращаем умерших к жизни так сказать.
            if (tp.getPlayerType() instanceof LosePlayer) {
                tp.setPlayerType(new PlayingPlayer());
            }
            
            tp.getPlayerType().preparePlayer(tp);
        }
    }
    
    @Override
    public void autoUpdateHandler() {
        //Если мы набрали минимум то меняем статус
        if (this.arena.getAllPlayersType(PlayingPlayer.class).size() >= this.arena.getMinPlayers()) {
            this.arena.setState(new StartingState(this.arena));
        }
        
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            if (!this.arena.getArenaRegion().isInRegion(tp.getPlayer().getLocation())) {
                tp.getPlayerType().preparePlayer(tp);
            }
        }
    }


}

package ru.boomearo.blockhunt.objects.state;

import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.playertype.WaitingPlayer;
import ru.boomearo.gamecontrol.objects.states.IWaitingState;

public class WaitingState implements IWaitingState, AllowJoin {

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
        
        //Делаем всех игроков ожидающими
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            tp.setPlayerType(new WaitingPlayer());
            
            tp.getPlayerType().preparePlayer(tp);
        }
    }
    
    @Override
    public void autoUpdateHandler() {
        //Если мы набрали минимум то меняем статус
        if (this.arena.getAllPlayers().size() >= this.arena.getMinPlayers()) {
            this.arena.setState(new StartingState(this.arena));
        }
        
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            if (!this.arena.getLobbyRegion().isInRegion(tp.getPlayer().getLocation())) {
                tp.getPlayerType().preparePlayer(tp);
            }
        }
    }


}

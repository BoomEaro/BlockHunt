package ru.boomearo.blockhunt.objects.state;

import org.bukkit.entity.Player;

import me.libraryaddict.disguise.DisguiseAPI;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.playertype.HiderPlayer;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
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
        //Делаем всех игроков ожидающими
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            Player pl = tp.getPlayer();
            IPlayerType type = tp.getPlayerType();
            if (type instanceof HiderPlayer) {
                HiderPlayer hp = (HiderPlayer) type;
                this.arena.unmakeSolid(tp, hp);
            }
            
            if (DisguiseAPI.isDisguised(pl)) {
                DisguiseAPI.undisguiseToAll(pl);
            }
            
            tp.setPlayerType(new WaitingPlayer());
            
            tp.getPlayerType().preparePlayer(tp);
        }
        
        this.arena.sendMessages(BlockHuntManager.prefix + "Ожидание игроков..");
    }
    
    @Override
    public void autoUpdateHandler() {
        //Если мы набрали минимум то меняем статус
        if (this.arena.getAllPlayers().size() >= this.arena.getMinPlayers()) {
            this.arena.setState(new StartingState(this.arena));
        }
        
        for (BHPlayer tp : this.arena.getAllPlayers()) {
            tp.getPlayer().spigot().respawn();
            
            if (!this.arena.getLobbyRegion().isInRegionPoint(tp.getPlayer().getLocation())) {
                tp.getPlayerType().preparePlayer(tp);
            }
        }
    }


}

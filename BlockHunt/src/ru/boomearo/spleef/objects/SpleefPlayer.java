package ru.boomearo.spleef.objects;

import org.bukkit.entity.Player;

import ru.boomearo.gamecontrol.objects.IGamePlayer;
import ru.boomearo.spleef.objects.playertype.IPlayerType;

public class SpleefPlayer implements IGamePlayer {

    private final String name;
    private final Player player;
    
    private IPlayerType playerType;
   
    private SpleefArena where;
    private SpleefTeam team;
    
    public SpleefPlayer(String name, Player player, IPlayerType playerType, SpleefArena where, SpleefTeam team) {
        this.name = name;
        this.player = player;
        this.playerType = playerType;
        this.where = where;
        this.team = team;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public Player getPlayer() {
        return this.player;
    }
    
    @Override
    public SpleefArena getArena() {
        return this.where;
    }
    
    public SpleefTeam getTeam() {
        return this.team;
    }
    
    public IPlayerType getPlayerType() {
        return this.playerType;
    }
    
    public void setPlayerType(IPlayerType playerType) {
        this.playerType = playerType;
    }
    
    
}

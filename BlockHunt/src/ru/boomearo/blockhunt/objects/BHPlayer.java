package ru.boomearo.blockhunt.objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
import ru.boomearo.gamecontrol.objects.IGamePlayer;

public class BHPlayer implements IGamePlayer {

    private final String name;
    private final Player player;
    
    private IPlayerType playerType;
   
    private BHArena where;
    
    private Material choosenBlock = null;
    
    public BHPlayer(String name, Player player, IPlayerType playerType, BHArena where) {
        this.name = name;
        this.player = player;
        this.playerType = playerType;
        this.where = where;
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
    public BHArena getArena() {
        return this.where;
    }
    
    public IPlayerType getPlayerType() {
        return this.playerType;
    }
    
    public void setPlayerType(IPlayerType playerType) {
        this.playerType = playerType;
    }
    
    public Material getChoosenBlock() {
        return this.choosenBlock;
    }
    
    public void setChoosenBlock(Material mat) {
        this.choosenBlock = mat;
    }
    
}

package ru.boomearo.blockhunt.objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import ru.boomearo.blockhunt.board.BHPageList;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
import ru.boomearo.board.Board;
import ru.boomearo.board.exceptions.BoardException;
import ru.boomearo.board.objects.PlayerBoard;
import ru.boomearo.board.objects.boards.AbstractPageList;
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
    
    public void sendBoard(Integer index) {
        PlayerBoard pb = Board.getInstance().getBoardManager().getPlayerBoard(this.name);
        if (pb != null) {
            try {
                AbstractPageList apl;
                if (index == null) {
                    apl = Board.getInstance().getBoardManager().getPageListFactory().createPageList(pb);
                }
                else {
                    apl = new BHPageList(pb, this);
                }
                
                pb.setNewPageList(apl);
                
                if (index != null) {
                    pb.toPage(index, pb.getPageByIndex(index));
                }
            } 
            catch (BoardException e) {
                e.printStackTrace();
            }
        }
    }
    
}

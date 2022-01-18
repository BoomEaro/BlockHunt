package ru.boomearo.blockhunt.board;

import java.util.ArrayList;
import java.util.List;

import ru.boomearo.blockhunt.board.pages.BHGamePage;
import ru.boomearo.blockhunt.board.pages.BHLobbyPage;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.board.objects.PlayerBoard;
import ru.boomearo.board.objects.boards.AbstractPage;
import ru.boomearo.board.objects.boards.AbstractPageList;

public class BHPageList extends AbstractPageList {

    private final BHPlayer bwPlayer;
    
    public BHPageList(PlayerBoard player, BHPlayer bwPlayer) {
        super(player);
        this.bwPlayer = bwPlayer;
    }

    @Override
    protected List<AbstractPage> createPages() {
        List<AbstractPage> pages = new ArrayList<AbstractPage>();
        
        pages.add(new BHLobbyPage(this, this.bwPlayer));
        pages.add(new BHGamePage(this, this.bwPlayer));
        
        return pages;
    }

}

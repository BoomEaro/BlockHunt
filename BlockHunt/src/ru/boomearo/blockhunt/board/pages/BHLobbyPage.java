package ru.boomearo.blockhunt.board.pages;

import java.util.ArrayList;
import java.util.List;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.statistics.BHStatsData;
import ru.boomearo.blockhunt.objects.statistics.BHStatsType;
import ru.boomearo.board.objects.boards.AbstractHolder;
import ru.boomearo.board.objects.boards.AbstractPage;
import ru.boomearo.board.objects.boards.AbstractPageList;
import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;

public class BHLobbyPage extends AbstractPage {

    private final BHPlayer bhPlayer;
    
    public BHLobbyPage(AbstractPageList pageList, BHPlayer bhPlayer) {
        super(pageList);
        this.bhPlayer = bhPlayer;
    }

    @Override
    public int getTimeToChange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public String getTitle() {
        return BlockHuntManager.gameNameDys;
    }

    @Override
    protected List<AbstractHolder> createHolders() {
        List<AbstractHolder> holders = new ArrayList<AbstractHolder>();
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return "§bАрена: '§e" + bhPlayer.getArena().getName() + "§b'";
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return " ";
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return "§bИгроков: §e" + bhPlayer.getArena().getAllPlayers().size() + "§7/§c" + bhPlayer.getArena().getMaxPlayers();
            }
            
            @Override
            public long getMaxCacheTime() {
                return 0;
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return "§bСтатус: " + bhPlayer.getArena().getState().getName();
            }
            
            @Override
            public long getMaxCacheTime() {
                return 0;
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return " ";
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return "§bСтатистика: ";
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return getStatisticData(BHStatsType.SeekersWin, bhPlayer.getName());
            }
            
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return getStatisticData(BHStatsType.HidersWin, bhPlayer.getName());
            }
            
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return getStatisticData(BHStatsType.SeekersKills, bhPlayer.getName());
            }
            
        });
        
        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return getStatisticData(BHStatsType.HidersKills, bhPlayer.getName());
            }
            
        });
        
        return holders;
    }

    private static String getStatisticData(BHStatsType type, String name) {
        BHStatsData data = BlockHunt.getInstance().getBlockHuntManager().getStatisticManager().getStatsData(type);
        StatsPlayer sp = data.getStatsPlayer(name);
        if (sp == null) {
            return "§b" + type.getName() + ": §e0";
        }
        
        return "§b" + type.getName() + ": §e" + (long) sp.getValue();
    }
    
}

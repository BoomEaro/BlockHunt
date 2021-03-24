package ru.boomearo.blockhunt.board.pages;

import java.util.ArrayList;
import java.util.List;

import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.playertype.HiderPlayer;
import ru.boomearo.blockhunt.objects.playertype.SeekerPlayer;
import ru.boomearo.blockhunt.objects.state.EndingState;
import ru.boomearo.blockhunt.objects.state.RunningState;
import ru.boomearo.board.objects.boards.AbstractHolder;
import ru.boomearo.board.objects.boards.AbstractPage;
import ru.boomearo.board.objects.boards.AbstractPageList;
import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.gamecontrol.utils.DateUtil;

public class BHGamePage extends AbstractPage {

    private final BHPlayer bhPlayer;
    
    public BHGamePage(AbstractPageList pageList, BHPlayer bhPlayer) {
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
                IGameState state = bhPlayer.getArena().getState();
                if (state instanceof RunningState) {
                    RunningState rs = (RunningState) state;
                    
                    return "§bИгра закончится через: §e" + DateUtil.formatedTime(rs.getCount(), false, true);
                }
                else if (state instanceof EndingState) {
                    EndingState es = (EndingState) state;
                    return "§bНовая игра через: §e" + DateUtil.formatedTime(es.getCount(), false, true);
                }
                return " ";
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
                return "§cСикеров: §e" + bhPlayer.getArena().getAllPlayersType(SeekerPlayer.class).size();
            }
            
            @Override
            public long getMaxCacheTime() {
                return 0;
            }

        });

        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return "§3Хайдеров: §e" + bhPlayer.getArena().getAllPlayersType(HiderPlayer.class).size();
            }
            
            @Override
            public long getMaxCacheTime() {
                return 0;
            }

        });
        
        return holders;
    }

}

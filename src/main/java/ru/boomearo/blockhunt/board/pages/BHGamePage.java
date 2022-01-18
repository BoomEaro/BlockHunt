package ru.boomearo.blockhunt.board.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        List<AbstractHolder> holders = new ArrayList<>();

        holders.add(new AbstractHolder(this) {

            @Override
            public String getText() {
                return BlockHuntManager.mainColor + new SimpleDateFormat("dd/MM/yyyy").format(new Date(System.currentTimeMillis()));
            }

        });

        holders.add(new AbstractHolder(this) {

            @Override
            public String getText() {
                return " ";
            }

        });

        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return BlockHuntManager.mainColor + "Карта: '" + BlockHuntManager.variableColor + bhPlayer.getArena().getName() + BlockHuntManager.mainColor + "'";
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
                return BlockHuntManager.mainColor + "Статус: " + bhPlayer.getArena().getState().getName();
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
                if (state instanceof RunningState rs) {

                    return BlockHuntManager.mainColor + "До конца: " + BlockHuntManager.variableColor + getFormattedTimeLeft(rs.getCount());
                }
                else if (state instanceof EndingState es) {
                    return BlockHuntManager.mainColor + "Новая игра: " + BlockHuntManager.variableColor + getFormattedTimeLeft(es.getCount());
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
                return BlockHuntManager.seekerColor + "Сикеров: " + BlockHuntManager.variableColor + bhPlayer.getArena().getAllPlayersType(SeekerPlayer.class).size();
            }

            @Override
            public long getMaxCacheTime() {
                return 0;
            }

        });

        holders.add(new AbstractHolder(this) {

            @Override
            protected String getText() {
                return BlockHuntManager.hiderColor + "Хайдеров: " + BlockHuntManager.variableColor + bhPlayer.getArena().getAllPlayersType(HiderPlayer.class).size();
            }

            @Override
            public long getMaxCacheTime() {
                return 0;
            }

        });

        return holders;
    }

    private static String getFormattedTimeLeft(int time) {
        int min = 0;
        int sec = 0;
        String minStr = "";
        String secStr = "";

        min = (int) Math.floor(time / 60);
        sec = time % 60;

        minStr = (min < 10) ? "0" + String.valueOf(min) : String.valueOf(min);
        secStr = (sec < 10) ? "0" + String.valueOf(sec) : String.valueOf(sec);

        return minStr + ":" + secStr;
    }
}

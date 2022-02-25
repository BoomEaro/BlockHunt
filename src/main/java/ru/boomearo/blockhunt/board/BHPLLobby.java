package ru.boomearo.blockhunt.board;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.BHStatsType;
import ru.boomearo.board.objects.PlayerBoard;
import ru.boomearo.board.objects.boards.AbstractHolder;
import ru.boomearo.board.objects.boards.AbstractPage;
import ru.boomearo.board.objects.boards.AbstractPageList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BHPLLobby extends AbstractPageList {

    private final BHPlayer bwPlayer;

    public BHPLLobby(PlayerBoard player, BHPlayer bwPlayer) {
        super(player);
        this.bwPlayer = bwPlayer;
    }

    @Override
    protected List<AbstractPage> createPages() {
        return List.of(new BHLobbyPage(this, this.bwPlayer));
    }

    public static class BHLobbyPage extends AbstractPage {

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
                    return BlockHuntManager.mainColor + "Игроков: " + BlockHuntManager.variableColor + bhPlayer.getArena().getAllPlayers().size() + "§8/" + BlockHuntManager.otherColor + bhPlayer.getArena().getMaxPlayers();
                }

                @Override
                public long getMaxCacheTime() {
                    return 0;
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
                    return " ";
                }

            });

            holders.add(new AbstractHolder(this) {

                @Override
                protected String getText() {
                    return BlockHuntManager.mainColor + "Статистика: ";
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
            double value = BlockHunt.getInstance().getBlockHuntManager().getStatisticManager().getStatsValueFromPlayer(type, name);
            return BlockHuntManager.mainColor + type.getName() + ": " + BlockHuntManager.variableColor + (long) value;
        }

    }

}

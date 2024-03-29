package ru.boomearo.blockhunt.managers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.disguise.DisguiseAPI;

import net.md_5.bungee.api.ChatColor;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.playertype.HiderPlayer;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
import ru.boomearo.blockhunt.objects.playertype.WaitingPlayer;
import ru.boomearo.blockhunt.objects.state.AllowJoin;
import ru.boomearo.blockhunt.objects.BHStatsType;
import ru.boomearo.board.Board;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.exceptions.GameControlException;
import ru.boomearo.gamecontrol.exceptions.PlayerGameException;
import ru.boomearo.gamecontrol.objects.IGameManager;
import ru.boomearo.gamecontrol.objects.defactions.IDefaultAction;
import ru.boomearo.gamecontrol.objects.states.game.IGameState;
import ru.boomearo.gamecontrol.objects.statistics.DefaultStatsManager;

public final class BlockHuntManager implements IGameManager<BHPlayer> {

    private final ConcurrentMap<String, BHArena> arenas = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, BHPlayer> players = new ConcurrentHashMap<>();

    private final DefaultStatsManager stats = new DefaultStatsManager(this, BHStatsType.values());

    public static final ChatColor mainColor = ChatColor.of(new Color(0, 255, 222));
    public static final ChatColor variableColor = ChatColor.of(new Color(250, 248, 82));
    public static final ChatColor otherColor = ChatColor.of(new Color(255, 60, 0));

    public static final ChatColor hiderColor = ChatColor.of(new Color(124, 207, 196));
    public static final ChatColor seekerColor = ChatColor.of(new Color(253, 134, 134));

    public static final String gameNameDys = "§8[" + variableColor + "BlockHunt§8]";
    public static final String prefix = gameNameDys + ": " + mainColor;

    public static final double hiderWinReward = 35;
    public static final double hiderKillReward = 5;

    public BlockHuntManager() {
        loadArenas();
    }

    @Override
    public String getGameName() {
        return "BlockHunt";
    }

    @Override
    public String getGameDisplayName() {
        return gameNameDys;
    }

    @Override
    public ChatColor getMainColor() {
        return mainColor;
    }

    @Override
    public ChatColor getVariableColor() {
        return variableColor;
    }

    @Override
    public ChatColor getOtherColor() {
        return otherColor;
    }

    @Override
    public JavaPlugin getPlugin() {
        return BlockHunt.getInstance();
    }

    @Override
    public BHPlayer join(Player pl, String arena, IDefaultAction action) throws ConsoleGameException, PlayerGameException {
        if (pl == null || arena == null) {
            throw new ConsoleGameException("Аргументы не должны быть нулем!");
        }

        BHPlayer tmpPlayer = this.players.get(pl.getName());
        if (tmpPlayer != null) {
            throw new ConsoleGameException("Игрок уже в игре!");
        }

        BHArena tmpArena = this.arenas.get(arena);
        if (tmpArena == null) {
            throw new PlayerGameException(mainColor + "Карта '" + variableColor + arena + mainColor + "' не найдена!");
        }

        ViaAPI api = Via.getAPI();
        int version = api.getPlayerVersion(pl);

        if (version > 754) {
            throw new PlayerGameException(mainColor + "Сожалеем, но для игры в эту мини-игру требуется версия игры 1.16.5.");
        }

        int count = tmpArena.getAllPlayers().size();
        if (count >= tmpArena.getMaxPlayers()) {
            throw new PlayerGameException(mainColor + "Карта '" + variableColor + arena + mainColor + "' переполнена!");
        }

        IGameState state = tmpArena.getState();

        if (!(state instanceof AllowJoin)) {
            throw new PlayerGameException(mainColor + "В карте '" + variableColor + arena + mainColor + "' уже идет игра!");
        }


        action.performDefaultJoinAction(pl);

        WaitingPlayer type = new WaitingPlayer();

        //Создаем игрока
        BHPlayer newTp = new BHPlayer(pl.getName(), pl, type, tmpArena);

        //Добавляем в арену
        tmpArena.addPlayer(newTp);

        //Добавляем в список играющих
        this.players.put(pl.getName(), newTp);

        //Обрабатываем игрока
        type.preparePlayer(newTp);

        pl.sendMessage(prefix + "Вы присоединились к карте " + mainColor + "'" + variableColor + arena + mainColor + "'!");
        pl.sendMessage(prefix + "Чтобы покинуть игру, используйте " + variableColor + "Магма крем " + mainColor + "или команду " + variableColor + "/lobby" + variableColor + ".");

        int currCount = tmpArena.getAllPlayers().size();
        if (currCount < tmpArena.getMinPlayers()) {
            pl.sendMessage(prefix + "Ожидание " + variableColor + (tmpArena.getMinPlayers() - currCount) + mainColor + " игроков для начала игры...");
        }

        tmpArena.sendMessages(prefix + pl.getDisplayName() + mainColor + " присоединился к игре! " + getRemainPlayersArena(tmpArena, null), pl.getName());

        return newTp;
    }

    @Override
    public void leave(Player pl, IDefaultAction action) throws ConsoleGameException, PlayerGameException {
        if (pl == null) {
            throw new ConsoleGameException("Аргументы не должны быть нулем!");
        }

        BHPlayer tmpPlayer = this.players.get(pl.getName());
        if (tmpPlayer == null) {
            throw new ConsoleGameException("Игрок не в игре!");
        }

        BHArena arena = tmpPlayer.getArena();

        arena.removePlayer(pl.getName());

        this.players.remove(pl.getName());

        Board.getInstance().getBoardManager().sendBoardToPlayer(tmpPlayer.getName(), null);

        //Снимаем свою твердую маскировку
        IPlayerType type = tmpPlayer.getPlayerType();
        if (type instanceof HiderPlayer hp) {
            arena.unmakeSolid(tmpPlayer, hp);
        }
        //Делаем игрока обычного
        if (DisguiseAPI.isDisguised(pl)) {
            DisguiseAPI.undisguiseToAll(pl);
        }

        //Показываем для себя всех замаскированных твердых а так же сбрасывает блоки
        arena.unmakeSolidAll(tmpPlayer);

        pl.sendMessage(prefix + "Вы покинули игру!");

        arena.sendMessages(prefix + pl.getDisplayName() + mainColor + " покинул игру! " + getRemainPlayersArena(arena, null), pl.getName());

        action.performDefaultLeaveAction(pl);
    }


    @Override
    public BHPlayer getGamePlayer(String name) {
        return this.players.get(name);
    }

    @Override
    public BHArena getGameArena(String name) {
        return this.arenas.get(name);
    }

    @Override
    public Collection<BHArena> getAllArenas() {
        return this.arenas.values();
    }

    @Override
    public Collection<BHPlayer> getAllPlayers() {
        return this.players.values();
    }

    @Override
    public DefaultStatsManager getStatisticManager() {
        return this.stats;
    }

    public BHArena getArenaByLocation(Location loc) {
        for (BHArena ar : BlockHunt.getInstance().getBlockHuntManager().getAllArenas()) {
            //Лобби тоже является частью арены
            if (ar.getArenaRegion().isInRegionPoint(loc) || (ar.getLobbyRegion() != null ? ar.getLobbyRegion().isInRegionPoint(loc) : false)) {
                return ar;
            }
        }
        return null;
    }

    public void loadArenas() {

        FileConfiguration fc = BlockHunt.getInstance().getConfig();
        List<BHArena> arenas = (List<BHArena>) fc.getList("arenas");
        if (arenas != null) {
            for (BHArena ar : arenas) {
                try {
                    addArena(ar);
                }
                catch (GameControlException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveArenas() {
        FileConfiguration fc = BlockHunt.getInstance().getConfig();

        List<BHArena> tmp = new ArrayList<>(this.arenas.values());
        fc.set("arenas", tmp);

        BlockHunt.getInstance().saveConfig();
    }

    public void addArena(BHArena arena) throws ConsoleGameException {
        if (arena == null) {
            throw new ConsoleGameException("Арена не может быть нулем!");
        }

        BHArena tmpArena = this.arenas.get(arena.getName());
        if (tmpArena != null) {
            throw new ConsoleGameException("Арена " + arena.getName() + " уже создана!");
        }

        this.arenas.put(arena.getName(), arena);
    }

    public void removeArena(String name) throws ConsoleGameException {
        if (name == null) {
            throw new ConsoleGameException("Название не может быть нулем!");
        }

        BHArena tmpArena = this.arenas.get(name);
        if (tmpArena == null) {
            throw new ConsoleGameException("Арена " + name + " не найдена!");
        }

        this.arenas.remove(name);
    }

    public static String getRemainPlayersArena(BHArena arena, Class<? extends IPlayerType> clazz) {
        return "§8[" + variableColor + (clazz != null ? arena.getAllPlayersType(clazz).size() : arena.getAllPlayers().size()) + mainColor + "/" + otherColor + arena.getMaxPlayers() + "§8]";
    }
}

package ru.boomearo.blockhunt.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.SpleefTeam;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
import ru.boomearo.blockhunt.objects.playertype.LosePlayer;
import ru.boomearo.blockhunt.objects.playertype.PlayingPlayer;
import ru.boomearo.blockhunt.objects.state.SpectatorFirst;
import ru.boomearo.blockhunt.utils.ExpFix;
import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.exceptions.GameControlException;
import ru.boomearo.gamecontrol.exceptions.PlayerGameException;
import ru.boomearo.gamecontrol.objects.IGameManager;
import ru.boomearo.gamecontrol.objects.states.IGameState;

public final class BlockHuntManager implements IGameManager {

    private final ConcurrentMap<String, BHArena> arenas = new ConcurrentHashMap<String, BHArena>();

    private final ConcurrentMap<String, BHPlayer> players = new ConcurrentHashMap<String, BHPlayer>();
    
    private final BlockHuntStatistics stats = new BlockHuntStatistics();
    
    public static final String gameNameDys = "§8[§bSpleef§8]";
    public static final String prefix = gameNameDys + ": §7";
    
    public static final double winReward = 5;

    public BlockHuntManager() {
        loadArenas();  
    }

    @Override
    public String getGameName() {
        return "Spleef";
    }

    @Override
    public String getGameDisplayName() {
        return gameNameDys;
    }

    @Override
    public JavaPlugin getPlugin() {
        return BlockHunt.getInstance();
    }

    @Override
    public BHPlayer join(Player pl, String arena) throws ConsoleGameException, PlayerGameException {
        if (pl == null || arena == null) {
            throw new ConsoleGameException("Аргументы не должны быть нулем!");
        }

        BHPlayer tmpPlayer = this.players.get(pl.getName());
        if (tmpPlayer != null) {
            throw new ConsoleGameException("Игрок уже в игре!");
        }

        BHArena tmpArena = this.arenas.get(arena);
        if (tmpArena == null) {
            throw new PlayerGameException("Арена §7'§b" + arena + "§7' не найдена!");
        }

        int count = tmpArena.getAllPlayers().size();
        if (count >= tmpArena.getMaxPlayers()) {
            throw new PlayerGameException("Арена §7'§b" + arena + "§7' переполнена!");
        }
        
        SpleefTeam team = tmpArena.getFreeTeam();
        if (team == null) {
            throw new ConsoleGameException("Не найдено свободных команд!");
        }
        
        IGameState state = tmpArena.getState();

        IPlayerType type;
        
        boolean isSpec;
        
        //Если статус игры реализует это, значит добавляем игрока в наблюдатели сначала
        if (state instanceof SpectatorFirst) {
            type = new LosePlayer();
            isSpec = true;
        }
        else {
            type = new PlayingPlayer();
            isSpec = false;
        }

        //Создаем игрока
        BHPlayer newTp = new BHPlayer(pl.getName(), pl, type, tmpArena, team);

        //Добавляем в команду
        team.setPlayer(newTp);
        
        //Добавляем в арену
        tmpArena.addPlayer(newTp);

        //Добавляем в список играющих
        this.players.put(pl.getName(), newTp);

        //Обрабатываем игрока
        type.preparePlayer(newTp);
        
        if (isSpec) {
            pl.sendMessage(prefix + "Вы присоединились к арене §7'§b" + arena + "§7' как наблюдатель.");
            pl.sendMessage(prefix + "Чтобы покинуть игру, используйте несколько раз §bкнопку §7'§b1§7' или §bтелепортируйтесь к любому игроку §7используя возможность наблюдателя.");
            
            tmpArena.sendMessages(prefix + "Игрок §b" + pl.getName() + " §7присоединился к игре как наблюдатель!");
        }
        else {
            pl.sendMessage(prefix + "Вы присоединились к арене §7'§b" + arena + "§7'!");
            pl.sendMessage(prefix + "Чтобы покинуть игру, используйте §bМагма крем §7или команду §b/spleef leave§7.");
            
            int currCount = tmpArena.getAllPlayersType(PlayingPlayer.class).size();
            if (currCount < tmpArena.getMinPlayers()) {
                pl.sendMessage(prefix + "Ожидание §b" + (tmpArena.getMinPlayers() - currCount) + " §7игроков для начала игры...");
            } 
            
            tmpArena.sendMessages(prefix + "Игрок §b" + pl.getName() + " §7присоединился к игре! " + getRemainPlayersArena(tmpArena), pl.getName());
        }
        
        return newTp;
    }

    @Override
    public void leave(Player pl) throws ConsoleGameException, PlayerGameException {
        if (pl == null) {
            throw new ConsoleGameException("Аргументы не должны быть нулем!");
        }

        BHPlayer tmpPlayer = this.players.get(pl.getName());
        if (tmpPlayer == null) {
            throw new ConsoleGameException("Игрок не в игре!");
        }

        SpleefTeam team = tmpPlayer.getTeam();
        
        //Удаляем у тимы игрока
        team.setPlayer(null);
        
        BHArena arena = tmpPlayer.getArena();
        
        arena.removePlayer(pl.getName());

        this.players.remove(pl.getName());

        if (Bukkit.isPrimaryThread()) {
            handlePlayerLeave(pl, arena);
        }
        else {
            Bukkit.getScheduler().runTask(BlockHunt.getInstance(), () -> {
                handlePlayerLeave(pl, arena);
            });
        }
    }

    private static void handlePlayerLeave(Player pl, BHArena arena) {
        Location loc = BlockHunt.getInstance().getEssentialsSpawn().getSpawn("default");
        if (loc != null) {
            GameControl.getInstance().asyncTeleport(pl, loc);
        }

        pl.setGameMode(GameMode.ADVENTURE);
        
        ExpFix.setTotalExperience(pl, 0);
        
        pl.getInventory().clear();
        
        pl.sendMessage(prefix + "Вы покинули игру!");
        
        arena.sendMessages(prefix + "Игрок §b" + pl.getName() + " §7покинул игру! " + getRemainPlayersArena(arena), pl.getName());
        
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
    public BlockHuntStatistics getStatisticManager() {
        return this.stats;
    }

    public BHArena getArenaByLocation(Location loc) {
        for (BHArena ar : BlockHunt.getInstance().getBlockHuntManager().getAllArenas()) {
            if (ar.getArenaRegion().isInRegion(loc)) {
                return ar;
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
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

        List<BHArena> tmp = new ArrayList<BHArena>(this.arenas.values());
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
    
    public static String getRemainPlayersArena(BHArena arena) {
        return "§8[§3" + arena.getAllPlayersType(PlayingPlayer.class).size() + "§7/§b" + arena.getMaxPlayers() + "§8]";
    }
}

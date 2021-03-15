package ru.boomearo.spleef.managers;

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

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.exceptions.GameControlException;
import ru.boomearo.gamecontrol.exceptions.PlayerGameException;
import ru.boomearo.gamecontrol.objects.IGameManager;
import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.objects.SpleefArena;
import ru.boomearo.spleef.objects.SpleefPlayer;
import ru.boomearo.spleef.objects.SpleefTeam;
import ru.boomearo.spleef.objects.playertype.IPlayerType;
import ru.boomearo.spleef.objects.playertype.LosePlayer;
import ru.boomearo.spleef.objects.playertype.PlayingPlayer;
import ru.boomearo.spleef.objects.state.SpectatorFirst;
import ru.boomearo.spleef.utils.ExpFix;

public final class SpleefManager implements IGameManager {

    private final ConcurrentMap<String, SpleefArena> arenas = new ConcurrentHashMap<String, SpleefArena>();

    private final ConcurrentMap<String, SpleefPlayer> players = new ConcurrentHashMap<String, SpleefPlayer>();
    
    private final SpleefStatistics stats = new SpleefStatistics();
    
    public static final String gameNameDys = "§8[§bSpleef§8]";
    public static final String prefix = gameNameDys + ": §7";
    
    public static final double winReward = 5;

    public SpleefManager() {
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
        return Spleef.getInstance();
    }

    @Override
    public SpleefPlayer join(Player pl, String arena) throws ConsoleGameException, PlayerGameException {
        if (pl == null || arena == null) {
            throw new ConsoleGameException("Аргументы не должны быть нулем!");
        }

        SpleefPlayer tmpPlayer = this.players.get(pl.getName());
        if (tmpPlayer != null) {
            throw new ConsoleGameException("Игрок уже в игре!");
        }

        SpleefArena tmpArena = this.arenas.get(arena);
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
        SpleefPlayer newTp = new SpleefPlayer(pl.getName(), pl, type, tmpArena, team);

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

        SpleefPlayer tmpPlayer = this.players.get(pl.getName());
        if (tmpPlayer == null) {
            throw new ConsoleGameException("Игрок не в игре!");
        }

        SpleefTeam team = tmpPlayer.getTeam();
        
        //Удаляем у тимы игрока
        team.setPlayer(null);
        
        SpleefArena arena = tmpPlayer.getArena();
        
        arena.removePlayer(pl.getName());

        this.players.remove(pl.getName());

        if (Bukkit.isPrimaryThread()) {
            handlePlayerLeave(pl, arena);
        }
        else {
            Bukkit.getScheduler().runTask(Spleef.getInstance(), () -> {
                handlePlayerLeave(pl, arena);
            });
        }
    }

    private static void handlePlayerLeave(Player pl, SpleefArena arena) {
        Location loc = Spleef.getInstance().getEssentialsSpawn().getSpawn("default");
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
    public SpleefPlayer getGamePlayer(String name) {
        return this.players.get(name);
    }

    @Override
    public SpleefArena getGameArena(String name) {
        return this.arenas.get(name);
    }
    
    @Override
    public Collection<SpleefArena> getAllArenas() {
        return this.arenas.values();
    }
    
    @Override
    public Collection<SpleefPlayer> getAllPlayers() {
        return this.players.values();
    }
    
    @Override
    public SpleefStatistics getStatisticManager() {
        return this.stats;
    }

    public SpleefArena getArenaByLocation(Location loc) {
        for (SpleefArena ar : Spleef.getInstance().getSpleefManager().getAllArenas()) {
            if (ar.getArenaRegion().isInRegion(loc)) {
                return ar;
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public void loadArenas() {

        FileConfiguration fc = Spleef.getInstance().getConfig();
        List<SpleefArena> arenas = (List<SpleefArena>) fc.getList("arenas");
        if (arenas != null) {
            for (SpleefArena ar : arenas) {
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
        FileConfiguration fc = Spleef.getInstance().getConfig();

        List<SpleefArena> tmp = new ArrayList<SpleefArena>(this.arenas.values());
        fc.set("arenas", tmp);

        Spleef.getInstance().saveConfig();
    }

    public void addArena(SpleefArena arena) throws ConsoleGameException {
        if (arena == null) {
            throw new ConsoleGameException("Арена не может быть нулем!");
        }

        SpleefArena tmpArena = this.arenas.get(arena.getName());
        if (tmpArena != null) {
            throw new ConsoleGameException("Арена " + arena.getName() + " уже создана!");
        }

        this.arenas.put(arena.getName(), arena);
    }

    public void removeArena(String name) throws ConsoleGameException {
        if (name == null) {
            throw new ConsoleGameException("Название не может быть нулем!");
        }

        SpleefArena tmpArena = this.arenas.get(name);
        if (tmpArena == null) {
            throw new ConsoleGameException("Арена " + name + " не найдена!");
        }

        this.arenas.remove(name);
    }
    
    public static String getRemainPlayersArena(SpleefArena arena) {
        return "§8[§3" + arena.getAllPlayersType(PlayingPlayer.class).size() + "§7/§b" + arena.getMaxPlayers() + "§8]";
    }
}

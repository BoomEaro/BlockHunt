package ru.boomearo.blockhunt.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.disguise.DisguiseAPI;
import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.playertype.HiderPlayer;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
import ru.boomearo.blockhunt.objects.playertype.WaitingPlayer;
import ru.boomearo.blockhunt.objects.state.AllowJoin;
import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.exceptions.GameControlException;
import ru.boomearo.gamecontrol.exceptions.PlayerGameException;
import ru.boomearo.gamecontrol.objects.IGameManager;
import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.gamecontrol.utils.ExpFix;

public final class BlockHuntManager implements IGameManager {

    private final ConcurrentMap<String, BHArena> arenas = new ConcurrentHashMap<String, BHArena>();

    private final ConcurrentMap<String, BHPlayer> players = new ConcurrentHashMap<String, BHPlayer>();
    
    private final BlockHuntStatistics stats = new BlockHuntStatistics();
    
    public static final String gameNameDys = "§8[§9BlockHunt§8]";
    public static final String prefix = gameNameDys + ": §b";
    
    public static final double hiderWinReward = 5;
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
            throw new PlayerGameException("Арена §b'§e" + arena + "§b' не найдена!");
        }

        int count = tmpArena.getAllPlayers().size();
        if (count >= tmpArena.getMaxPlayers()) {
            throw new PlayerGameException("Арена §b'§e" + arena + "§b' переполнена!");
        }

        IGameState state = tmpArena.getState();

        if (!(state instanceof AllowJoin)) {
            throw new PlayerGameException("В арене §b'§e" + arena + "§b' уже идет игра!");
        }
        
        WaitingPlayer type = new WaitingPlayer();

        //Создаем игрока
        BHPlayer newTp = new BHPlayer(pl.getName(), pl, type, tmpArena);
        
        //Добавляем в арену
        tmpArena.addPlayer(newTp);

        //Добавляем в список играющих
        this.players.put(pl.getName(), newTp);

        //Обрабатываем игрока
        type.preparePlayer(newTp);
        
        pl.sendMessage(prefix + "Вы присоединились к арене §b'§e" + arena + "§b'!");
        pl.sendMessage(prefix + "Чтобы покинуть игру, используйте §eМагма крем §bили команду §e/bh leave§b.");
        
        int currCount = tmpArena.getAllPlayers().size();
        if (currCount < tmpArena.getMinPlayers()) {
            pl.sendMessage(prefix + "Ожидание §e" + (tmpArena.getMinPlayers() - currCount) + " §bигроков для начала игры...");
        } 
        
        tmpArena.sendMessages(prefix + "§e" + pl.getDisplayName() + " §bприсоединился к игре! " + getRemainPlayersArena(tmpArena, null), pl.getName());
        
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
        
        BHArena arena = tmpPlayer.getArena();
        
        arena.removePlayer(pl.getName());

        this.players.remove(pl.getName());

        if (Bukkit.isPrimaryThread()) {
            handlePlayerLeave(tmpPlayer, arena);
        }
        else {
            Bukkit.getScheduler().runTask(BlockHunt.getInstance(), () -> {
                handlePlayerLeave(tmpPlayer, arena);
            });
        }
    }

    private static void handlePlayerLeave(BHPlayer player, BHArena arena) {
        player.sendBoard(null);
        
        Player pl = player.getPlayer();
        
        Location loc = GameControl.getSpawnLocation();
        if (loc != null) {
            pl.teleport(loc);
        }

        pl.setGameMode(GameMode.ADVENTURE);
        
        pl.setFoodLevel(20);
        
        pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        
        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        
        ExpFix.setTotalExperience(pl, 0);
        
        pl.getInventory().clear();
        
        //Снимаем свою твердую маскировку
        IPlayerType type = player.getPlayerType();
        if (type instanceof HiderPlayer) {
            HiderPlayer hp = (HiderPlayer) type;
            arena.unmakeSolid(player, hp);
        }
        //Делаем игрока обычного
        if (DisguiseAPI.isDisguised(pl)) {
            DisguiseAPI.undisguiseToAll(pl);
        }
        
        //Показываем для себя всех замаскированных твердых а так же сбрасывает блоки
        arena.unmakeSolidAll(player);

        pl.sendMessage(prefix + "Вы покинули игру!");
        
        arena.sendMessages(prefix + "§e" + pl.getDisplayName() + " §bпокинул игру! " + getRemainPlayersArena(arena, null), pl.getName());
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
            //Лобби тоже является частью арены
            if (ar.getArenaRegion().isInRegionPoint(loc) || (ar.getLobbyRegion() != null ? ar.getLobbyRegion().isInRegionPoint(loc) : false)) {
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
    
    public static String getRemainPlayersArena(BHArena arena, Class<? extends IPlayerType> clazz) {
        return "§8[§e" + (clazz != null ? arena.getAllPlayersType(clazz).size() : arena.getAllPlayers().size()) + "§7/§c" + arena.getMaxPlayers() + "§8]";
    }
}

package ru.boomearo.blockhunt.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
import ru.boomearo.blockhunt.objects.state.WaitingState;
import ru.boomearo.gamecontrol.objects.IGameArena;
import ru.boomearo.gamecontrol.objects.IRegion;
import ru.boomearo.gamecontrol.objects.states.IGameState;

public class BHArena implements IGameArena, ConfigurationSerializable {

    private final String name;
    
    private final int minPlayers;
    private final int maxPlayers;
    private final int timelimit;
    
    private final World world;
    private final IRegion arenaRegion;
    
    private Location lobbyLocation;
    private Location seekersLocation;
    private Location hidersLocation;
    
    private volatile IGameState state = new WaitingState(this);
    
    private final ConcurrentMap<String, BHPlayer> players = new ConcurrentHashMap<String, BHPlayer>();
    
    public BHArena(String name, int minPlayers, int maxPlayers, int timeLimit, World world, IRegion arenaRegion, Location lobbyLocation, Location seekersLocation, Location hidersLocation) {
        this.name = name;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.timelimit = timeLimit;
        this.world = world;
        this.arenaRegion = arenaRegion;
        this.lobbyLocation = lobbyLocation;
        this.seekersLocation = seekersLocation;
        this.hidersLocation = hidersLocation;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public BHPlayer getGamePlayer(String name) {
        return this.players.get(name);
    }
    
    @Override
    public Collection<BHPlayer> getAllPlayers() {
        return this.players.values();
    }
    
    @Override
    public BlockHuntManager getManager() {
        return BlockHunt.getInstance().getBlockHuntManager();
    }
    
    @Override
    public IGameState getState() {
        return this.state;
    }
    
    @Override
    public void regen() {
        throw new UnsupportedOperationException("Данная арена не требуется в регенерации.");
    }
    
    public int getMinPlayers() {
        return this.minPlayers;
    }
    
    public int getMaxPlayers() {
        return this.maxPlayers;
    }
    
    public int getTimeLimit() {
        return this.timelimit;
    }
    
    public World getWorld() {
        return this.world;
    }
    
    public IRegion getArenaRegion() {
        return this.arenaRegion;
    }
    
    public Location getLobbyLocation() {
        return this.lobbyLocation;
    }
    
    public Location getSeekersLocation() {
        return this.seekersLocation;
    }
    
    public Location getHidersLocation() {
        return this.hidersLocation;
    }
    
    public void setLobbyLocation(Location loc) {
        this.lobbyLocation = loc;
    }
    
    public void setSeekersLocation(Location loc) {
        this.seekersLocation = loc;
    }
    
    public void setHidersLocation(Location loc) {
        this.hidersLocation = loc;
    }
    
    public void setState(IGameState state) {
        //Устанавливаем новое
        this.state = state;
        
        //Инициализируем новое
        this.state.initState();
    }
    
    public void addPlayer(BHPlayer player) {
        this.players.put(player.getName(), player);
    }
    
    public void removePlayer(String name) {
        this.players.remove(name);
    }
    
    public void sendMessages(String msg) {
        sendMessages(msg, null);
    }
    public void sendMessages(String msg, String ignore) {
        for (BHPlayer tp : this.players.values()) {
            if (ignore != null) {
                if (tp.getName().equals(ignore)) {
                    continue;
                }
            }
            
            Player pl = tp.getPlayer();
            if (pl.isOnline()) {
                pl.sendMessage(msg);
            }
        }
    }
    
    public void sendLevels(int level) {
        if (Bukkit.isPrimaryThread()) {
            handleSendLevels(level);
        }
        else {
            Bukkit.getScheduler().runTask(BlockHunt.getInstance(), () -> {
                handleSendLevels(level);
            });
        }
    }
    
    public void sendSounds(Sound sound, float volume, float pitch, Location loc) {
        for (BHPlayer tp : this.players.values()) {
            Player pl = tp.getPlayer();
            if (pl.isOnline()) {
                pl.playSound((loc != null ? loc : pl.getLocation()), sound, volume, pitch);
            }
        }
    }
    
    public void sendSounds(Sound sound, float volume, float pitch) {
        sendSounds(sound, volume, pitch, null);
    }
    
    private void handleSendLevels(int level) {
        for (BHPlayer tp : this.players.values()) {
            Player pl = tp.getPlayer();
            if (pl.isOnline()) {
                pl.setLevel(level);
            }
        }
    }
    
    public Collection<BHPlayer> getAllPlayersType(Class<? extends IPlayerType> clazz) {
        Set<BHPlayer> tmp = new HashSet<BHPlayer>();
        for (BHPlayer tp : this.players.values()) {
            if (tp.getPlayerType().getClass() == clazz) {
                tmp.add(tp);
            }
        }
        return tmp;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("name", this.name);
        result.put("minPlayers", this.minPlayers);
        result.put("maxPlayers", this.maxPlayers);
        result.put("timeLimit", this.timelimit);
        
        result.put("world", this.world.getName());
        result.put("region", this.arenaRegion);
        
        result.put("lobbyLocation", this.lobbyLocation);
        result.put("seekersLocation", this.seekersLocation);
        result.put("hidersLocation", this.hidersLocation);
        
        return result;
    }

    public static BHArena deserialize(Map<String, Object> args) {
        String name = null;
        int minPlayers = 2;
        int maxPlayers = 15;
        int timeLimit = 300;
        World world = null;
        IRegion region = null;
        Location lobbyLocation = null;
        Location seekersLocation = null;
        Location hidersLocation = null;

        Object na = args.get("name");
        if (na != null) {
            name = (String) na;
        }

        Object minp = args.get("minPlayers");
        if (minp != null) {
            minPlayers = ((Number) minp).intValue();
        }

        Object maxp = args.get("maxPlayers");
        if (maxp != null) {
            maxPlayers = ((Number) maxp).intValue();
        }

        Object tl = args.get("timeLimit");
        if (tl != null) {
            timeLimit = ((Number) tl).intValue();
        }
        
        Object wo = args.get("world");
        if (wo != null) {
            world = Bukkit.getWorld((String) wo);
        }

        Object re = args.get("region");
        if (re != null) {
            region = (IRegion) re;
        }

        Object l = args.get("lobbyLocation");
        if (l != null) {
            lobbyLocation = (Location) l;
        }
        
        Object s = args.get("seekersLocation");
        if (s != null) {
            seekersLocation = (Location) s;
        }
        
        Object h = args.get("hidersLocation");
        if (h != null) {
            hidersLocation = (Location) h;
        }
        
        return new BHArena(name, minPlayers, maxPlayers, timeLimit, world, region, lobbyLocation, seekersLocation, hidersLocation);
    }
}

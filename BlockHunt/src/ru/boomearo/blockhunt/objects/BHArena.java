package ru.boomearo.blockhunt.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.playertype.HiderPlayer;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
import ru.boomearo.blockhunt.objects.state.WaitingState;
import ru.boomearo.blockhunt.utils.RandomUtil;
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
    private IRegion lobbyRegion;
    
    private Location seekersLocation;
    private Location hidersLocation;
    
    private final List<Material> hideBlocks;
    
    private volatile IGameState state = new WaitingState(this);
    
    private final ConcurrentMap<String, BHPlayer> players = new ConcurrentHashMap<String, BHPlayer>();
    
    private final ConcurrentMap<String, SolidPlayer> hiddenLocs = new ConcurrentHashMap<String, SolidPlayer>();
    private final ConcurrentMap<String, SolidPlayer> hiddenPlayers = new ConcurrentHashMap<String, SolidPlayer>();
    
    public BHArena(String name, int minPlayers, int maxPlayers, int timeLimit, World world, IRegion arenaRegion, Location lobbyLocation, IRegion lobbyRegion, Location seekersLocation, Location hidersLocation, List<Material> hideBlocks) {
        this.name = name;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.timelimit = timeLimit;
        this.world = world;
        this.arenaRegion = arenaRegion;
        this.lobbyLocation = lobbyLocation;
        this.lobbyRegion = lobbyRegion;
        this.seekersLocation = seekersLocation;
        this.hidersLocation = hidersLocation;
        this.hideBlocks = hideBlocks;
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
    
    public IRegion getLobbyRegion() {
        return this.lobbyRegion;
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
    
    public void setLobbyRegion(IRegion region) {
        this.lobbyRegion = region; 
    }
    
    public void setSeekersLocation(Location loc) {
        this.seekersLocation = loc;
    }
    
    public void setHidersLocation(Location loc) {
        this.hidersLocation = loc;
    }
    
    public List<Material> getAllHideBlocks() {
        return this.hideBlocks;
    }
    
    public Material getRandomHideBlock() {
        if (this.hideBlocks.isEmpty()) {
            return Material.STONE;
        }
        
        return this.hideBlocks.get(RandomUtil.getRandomNumberRange(0, (this.hideBlocks.size() - 1)));
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
        result.put("lobbyRegion", this.lobbyRegion);
        
        result.put("seekersLocation", this.seekersLocation);
        result.put("hidersLocation", this.hidersLocation);
        
        List<String> bl = new ArrayList<String>();
        for (Material mat : this.hideBlocks) {
            bl.add(mat.name());
        }
        
        result.put("hideBlocks", bl);
        
        return result;
    }

    @SuppressWarnings("unchecked")
    public static BHArena deserialize(Map<String, Object> args) {
        String name = null;
        int minPlayers = 2;
        int maxPlayers = 15;
        int timeLimit = 300;
        World world = null;
        IRegion region = null;
        Location lobbyLocation = null;
        IRegion lobbyRegion = null;
        Location seekersLocation = null;
        Location hidersLocation = null;
        List<String> hideBlocks = new ArrayList<String>();

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
        
        Object lr = args.get("lobbyRegion");
        if (lr != null) {
            lobbyRegion = (IRegion) lr;
        }
        
        Object s = args.get("seekersLocation");
        if (s != null) {
            seekersLocation = (Location) s;
        }
        
        Object h = args.get("hidersLocation");
        if (h != null) {
            hidersLocation = (Location) h;
        }
        
        Object hb = args.get("hideBlocks");
        if (hb != null) {
            hideBlocks = (List<String>) hb;
        }
        
        List<Material> hiB = new ArrayList<Material>();
        for (String sss : hideBlocks) {
            Material mat = null;
            try {
                mat = Material.valueOf(sss);
            }
            catch (Exception e) {}
            if (mat == null) {
                continue;
            }
            
            hiB.add(mat);
        }
        
        return new BHArena(name, minPlayers, maxPlayers, timeLimit, world, region, lobbyLocation, lobbyRegion, seekersLocation, hidersLocation, hiB);
    }
    
    
    public void makeSolid(BHPlayer player, HiderPlayer hp, Block old) {
        Player pl = player.getPlayer();
        SolidPlayer bs = getSolidPlayerByLocation(old.getLocation());
        if (bs != null) {
            if (!bs.getPlayer().getName().equals(player.getName())) {
                pl.sendMessage(BlockHuntManager.prefix + "§cВ данном месте уже кто то замаскирован!");
            }
            return;
        }
        
        addSolidPlayer(new SolidPlayer(player, hp, old.getLocation(), old.getType()));
        
        if (DisguiseAPI.isDisguised(pl)) {
            DisguiseAPI.undisguiseToAll(pl);
        }
        
        for (BHPlayer pla : getAllPlayers()) {
            if (pla.getName().equals(player.getName())) {
                continue;
            }
            Player pp = pla.getPlayer();
            pp.hidePlayer(BlockHunt.getInstance(), player.getPlayer());
            pp.sendBlockChange(old.getLocation(), Bukkit.createBlockData(hp.getHideBlock()));
        }
        
        pl.sendMessage(BlockHuntManager.prefix + "Теперь вы твердый блок!");
        pl.playSound(player.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1.5f);

    }

    public void unmakeSolid(BHPlayer player, HiderPlayer hp) {
        hp.resetBlockCount();
        
        SolidPlayer bs = getSolidPlayerByPlayer(player.getName());
        if (bs == null) {
            return;
        }
        
        removeSolidPlayerByName(player.getName());
        
        for (BHPlayer pla : player.getArena().getAllPlayers()) {
            if (pla.getName().equals(player.getName())) {
                continue;
            }
            Player pp = pla.getPlayer();
            pp.showPlayer(BlockHunt.getInstance(), player.getPlayer());
            pp.sendBlockChange(bs.getLocation(), Bukkit.createBlockData(bs.getOldMaterial()));
        }
        
        Player pl = player.getPlayer();
        if (DisguiseAPI.isDisguised(pl)) {
            DisguiseAPI.undisguiseToAll(pl);
        }
        
        Disguise d = new MiscDisguise(DisguiseType.FALLING_BLOCK, hp.getHideBlock());
        d.setNotifyBar(null);
        DisguiseAPI.disguiseToAll(pl, d);
        
        pl.sendMessage(BlockHuntManager.prefix + "Вы больше не твердый блок.");
        pl.playSound(player.getPlayer().getLocation(), Sound.ENTITY_BAT_AMBIENT, 100, 1.5f);
    }
    
    //Показывает всех замаскированных игроков для этого игрока
    public void unmakeSolidAll(BHPlayer player) {
        Player pl = player.getPlayer();
        for (SolidPlayer sp : this.hiddenPlayers.values()) {
            Player pp = sp.getPlayer().getPlayer();
            
            pl.showPlayer(BlockHunt.getInstance(), pp);
            pl.sendBlockChange(sp.getLocation(), Bukkit.createBlockData(sp.getOldMaterial()));
        }
    }
    
    public SolidPlayer getSolidPlayerByLocation(Location loc) {
        return this.hiddenLocs.get(convertLocToString(loc));
    }
    
    public SolidPlayer getSolidPlayerByPlayer(String name) {
        return this.hiddenPlayers.get(name);
    }
    
    public void addSolidPlayer(SolidPlayer player) {
        this.hiddenLocs.put(convertLocToString(player.getLocation()), player);
        this.hiddenPlayers.put(player.getPlayer().getName(), player);
    }
    
    public void removeSolidPlayerByName(String name) {
        SolidPlayer sb = this.hiddenPlayers.get(name);
        if (sb == null) {
            return;
        }
        this.hiddenLocs.remove(convertLocToString(sb.getLocation()));
        this.hiddenPlayers.remove(name);
    }
    
    public Collection<SolidPlayer> getAllSolidPlayers() {
        return this.hiddenPlayers.values();
    }
    
    public static String convertLocToString(Location loc) {
        return loc.getBlockX() + "|" + loc.getBlockY() + "|" +  loc.getBlockZ();
    }
}

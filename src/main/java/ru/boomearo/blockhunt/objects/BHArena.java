package ru.boomearo.blockhunt.objects;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.playertype.HiderPlayer;
import ru.boomearo.blockhunt.objects.playertype.IPlayerType;
import ru.boomearo.blockhunt.objects.state.WaitingState;
import ru.boomearo.gamecontrol.objects.IForceStartable;
import ru.boomearo.gamecontrol.objects.arena.AbstractGameArena;
import ru.boomearo.gamecontrol.objects.region.IRegion;
import ru.boomearo.langhelper.LangHelper;
import ru.boomearo.langhelper.versions.LangType;
import ru.boomearo.serverutils.utils.other.RandomUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BHArena extends AbstractGameArena<BHPlayer> implements IForceStartable, ConfigurationSerializable {

    private final int minPlayers;
    private final int maxPlayers;
    private final int timeLimit;

    private final IRegion arenaRegion;

    private Location lobbyLocation;
    private IRegion lobbyRegion;

    private Location seekersLocation;
    private Location hidersLocation;

    private final List<Material> hideBlocks;

    private final ConcurrentMap<String, SolidPlayer> hiddenLocs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SolidPlayer> hiddenPlayers = new ConcurrentHashMap<>();

    private boolean forceStarted = false;

    public BHArena(String name, World world, Material icon, int minPlayers, int maxPlayers, int timeLimit, IRegion arenaRegion, Location lobbyLocation, IRegion lobbyRegion, Location seekersLocation, Location hidersLocation, List<Material> hideBlocks) {
        super(name, world, icon);
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.timeLimit = timeLimit;
        this.arenaRegion = arenaRegion;
        this.lobbyLocation = lobbyLocation;
        this.lobbyRegion = lobbyRegion;
        this.seekersLocation = seekersLocation;
        this.hidersLocation = hidersLocation;
        this.hideBlocks = hideBlocks;

        setState(new WaitingState(this));
    }

    @Override
    public boolean isForceStarted() {
        return this.forceStarted;
    }

    @Override
    public void setForceStarted(boolean force) {
        this.forceStarted = force;
    }

    @Override
    public BlockHuntManager getManager() {
        return BlockHunt.getInstance().getBlockHuntManager();
    }

    @Override
    public int getMinPlayers() {
        return this.minPlayers;
    }

    @Override
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public int getTimeLimit() {
        return this.timeLimit;
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

        return this.hideBlocks.get(RandomUtils.getRandomNumberRange(0, (this.hideBlocks.size() - 1)));
    }

    public Collection<BHPlayer> getAllPlayersType(Class<? extends IPlayerType> clazz) {
        Set<BHPlayer> tmp = new HashSet<>();
        for (BHPlayer tp : getAllPlayers()) {
            if (tp.getPlayerType().getClass() == clazz) {
                tmp.add(tp);
            }
        }
        return tmp;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("name", getName());
        result.put("icon", getIcon().name());
        result.put("minPlayers", this.minPlayers);
        result.put("maxPlayers", this.maxPlayers);
        result.put("timeLimit", this.timeLimit);

        result.put("world", getWorld().getName());
        result.put("region", this.arenaRegion);

        result.put("lobbyLocation", this.lobbyLocation);
        result.put("lobbyRegion", this.lobbyRegion);

        result.put("seekersLocation", this.seekersLocation);
        result.put("hidersLocation", this.hidersLocation);

        List<String> bl = new ArrayList<>();
        for (Material mat : this.hideBlocks) {
            bl.add(mat.name());
        }

        result.put("hideBlocks", bl);

        return result;
    }

    public static BHArena deserialize(Map<String, Object> args) {
        String name = null;
        Material icon = Material.STONE;
        int minPlayers = 2;
        int maxPlayers = 15;
        int timeLimit = 300;
        World world = null;
        IRegion region = null;
        Location lobbyLocation = null;
        IRegion lobbyRegion = null;
        Location seekersLocation = null;
        Location hidersLocation = null;
        List<String> hideBlocks = new ArrayList<>();

        Object na = args.get("name");
        if (na != null) {
            name = (String) na;
        }

        Object ic = args.get("icon");
        if (ic != null) {
            try {
                icon = Material.valueOf((String) ic);
            }
            catch (Exception ignored) {
            }
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

        List<Material> hiB = new ArrayList<>();
        for (String sss : hideBlocks) {
            Material mat = null;
            try {
                mat = Material.valueOf(sss);
            }
            catch (Exception ignored) {
            }
            if (mat == null) {
                continue;
            }

            hiB.add(mat);
        }

        return new BHArena(name, world, icon, minPlayers, maxPlayers, timeLimit, region, lobbyLocation, lobbyRegion, seekersLocation, hidersLocation, hiB);
    }


    public void makeSolid(BHPlayer player, HiderPlayer hp, Block old) {
        Player pl = player.getPlayer();
        SolidPlayer bs = getSolidPlayerByLocation(old.getLocation());
        if (bs != null) {
            if (!bs.getPlayer().getName().equals(player.getName())) {
                pl.sendMessage(BlockHuntManager.prefix + BlockHuntManager.hiderColor + "В данном месте уже кто то замаскирован!");
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

        pl.sendMessage(BlockHuntManager.prefix + "Теперь вы твердый блок " + BlockHuntManager.variableColor + LangHelper.getInstance().getItemTranslate(new ItemStack(hp.getHideBlock(), 1), LangType.RU_RU) + "§b!");
        pl.playSound(player.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 2f);

    }

    //TODO Похоже, плагин ViaVersion иногда не отправляет некоторые пакеты игрокам с других версий, из-за чего
    //TODO многие становятся невидимыми. Хз как и что фиксить.
    public void unmakeSolid(BHPlayer player, HiderPlayer hp) {
        hp.resetBlockCount();

        SolidPlayer bs = getSolidPlayerByPlayer(player.getName());
        if (bs == null) {
            return;
        }

        removeSolidPlayerByName(player.getName());

        for (BHPlayer pla : getAllPlayers()) {
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

        pl.sendMessage(BlockHuntManager.prefix + BlockHuntManager.hiderColor + "Вы больше не твердый блок " + BlockHuntManager.variableColor + LangHelper.getInstance().getItemTranslate(new ItemStack(hp.getHideBlock(), 1), LangType.RU_RU) + "§c.");
        pl.playSound(player.getPlayer().getLocation(), Sound.ENTITY_BAT_AMBIENT, 100, 1.3f);
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
        return loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ();
    }
}

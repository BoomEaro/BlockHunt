package ru.boomearo.blockhunt.objects.playertype;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.ItemButton;
import ru.boomearo.blockhunt.objects.state.RunningState;
import ru.boomearo.gamecontrol.utils.ExpFix;

public class HiderPlayer implements IPlayerType {
    
    private Material hideBlock = null;
    
    //сколько ждать секунд перед выдачей меча
    private int countSword = RunningState.hiderSwordTime;
    private int cdSword = 20;
    private boolean sword = false;
    
    private Location blockLoc = null;
    private int cdBlock = 20;
    private int countBlock = placesCdTime;

    public static final int placesCdTime = 5;
    
    @Override
    public void preparePlayer(BHPlayer player) {
        Player pl = player.getPlayer();
        
        pl.setFoodLevel(20);
        
        //Делаем хайдеру 10 хп
        pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(10);
        
        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        
        pl.setGameMode(GameMode.SURVIVAL);
        pl.setFlying(false);
        pl.setAllowFlight(false);
        
        pl.closeInventory();
        
        ExpFix.setTotalExperience(player.getPlayer(), 0);
        
        PlayerInventory inv = pl.getInventory();
        inv.clear();
        
        ItemButton leave = ItemButton.Leave;
        inv.setItem(leave.getSlot(), leave.getItem());
        
        inv.setItem(EquipmentSlot.HEAD, new ItemStack(this.hideBlock, 1));
        
        inv.setHeldItemSlot(0);
        
        player.sendBoard(1);
        
        Location loc = player.getArena().getHidersLocation();
        if (loc != null) {
            pl.teleport(loc);
        }
        
        if (DisguiseAPI.isDisguised(pl)) {
            DisguiseAPI.undisguiseToAll(pl);
        }
        
        Disguise d = new MiscDisguise(DisguiseType.FALLING_BLOCK, this.hideBlock);
        d.setNotifyBar(null);
        DisguiseAPI.disguiseToAll(pl, d);
    }
    
    public Material getHideBlock() {
        return this.hideBlock;
    }
    
    public void setHideBlock(Material block) {
        this.hideBlock = block;
    }
    
    public Location getBlockLocation() {
        return this.blockLoc;
    }
    
    public void setBlockLocation(Location loc) {
        this.blockLoc = loc;
    }
    
    public int getBlockCount() {
        return this.countBlock;
    }
    
    public void resetBlockCount() {
        this.countBlock = placesCdTime;
    }
    
    public void autoHandle(BHPlayer player) {
        handleDisguise(player);
        handleSwordCount(player);
    }
    
    private void handleDisguise(BHPlayer player) {
        BHArena arena = player.getArena();
        
        Player pl = player.getPlayer();
        Location curr = pl.getLocation();
        
        if (this.blockLoc == null) {
            this.blockLoc = curr;
            return;
        }
        
        //Если игрок находится в одном блоке
        if ((curr.getBlockX() == this.blockLoc.getBlockX()) && (curr.getBlockY() == this.blockLoc.getBlockY()) && (curr.getBlockZ() == this.blockLoc.getBlockZ())) {
            handleSolidCount(player, curr, arena);
        }
        else {
            arena.unmakeSolid(player, this);
        }
        
        this.blockLoc = curr;
    }
    
    private void handleSolidCount(BHPlayer player, Location curr, BHArena arena) {
        if (this.cdBlock <= 0) {
            this.cdBlock = 20;
            Player pl = player.getPlayer();
            
            if (this.countBlock <= 0) {

                Block block = curr.getBlock();
                Material t = block.getType();
                if (!(t == Material.AIR || t == Material.WATER)) {
                    pl.sendMessage(BlockHuntManager.prefix + "§cВы не можете стать твердым здесь!");
                    return;
                }
                
                arena.makeSolid(player, this, block);
                return;
            }
            
            
            pl.getInventory().setItem(6, new ItemStack(this.hideBlock, this.countBlock));
            
            //pl.sendMessage("До становления тведым блоком осталось " + this.countBlock);
            
            this.countBlock--;
        }
        this.cdBlock--;
    }
    
    private void handleSwordCount(BHPlayer player) {
        if (this.sword) {
            return;
        }
        
        if (this.cdSword <= 0) {
            this.cdSword = 20;
            
            Player pl = player.getPlayer();
            
            if (this.countSword <= 0) {
                this.sword = true;
                
                ItemButton ib = ItemButton.HiderSword;
                pl.getInventory().setItem(ib.getSlot(), ib.getItem());
                
                pl.sendMessage(BlockHuntManager.prefix + "Вам был выдан меч!");
                return;
            }

            
            this.countSword--;
        }
        this.cdSword--;
    }

}

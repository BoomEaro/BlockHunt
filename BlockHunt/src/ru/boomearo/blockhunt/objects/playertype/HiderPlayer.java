package ru.boomearo.blockhunt.objects.playertype;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.ItemButton;
import ru.boomearo.blockhunt.objects.state.RunningState;
import ru.boomearo.blockhunt.utils.ExpFix;
import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.objects.states.IGameState;

public class HiderPlayer implements IPlayerType {
    
    //сколько ждать секунд перед выдачей меча
    private int countSword = RunningState.hiderSwordTime;
    private int cdSword = 20;
    private boolean sword = false;
    
    private Location blockLoc = null;
    private int cdBlock = 20;
    private int countBlock = placesCdTime;
    private boolean solid = false;

    public static final int placesCdTime = 5;
    
    @Override
    public void preparePlayer(BHPlayer player) {
        Player pl = player.getPlayer();
        
        pl.setFoodLevel(20);
        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        
        pl.setGameMode(GameMode.SURVIVAL);
        pl.setFlying(false);
        pl.setAllowFlight(false);
        
        ExpFix.setTotalExperience(player.getPlayer(), 0);
        
        PlayerInventory inv = pl.getInventory();
        inv.clear();
        
        ItemButton leave = ItemButton.Leave;
        inv.setItem(leave.getSlot(), leave.getItem());
        
        inv.setHeldItemSlot(0);
        
        Location loc = player.getArena().getHidersLocation();
        if (loc != null) {
            GameControl.getInstance().asyncTeleport(pl, loc);
        }
    }
    
    public Location getBlockLocation() {
        return this.blockLoc;
    }
    
    public void setBlockLocation(Location loc) {
        this.blockLoc = loc;
    }
    
    
    public void autoHandle(BHPlayer player) {
        handleDisguise(player);
        handleSwordCount(player);
    }
    
    private void handleDisguise(BHPlayer player) {
        IGameState state = player.getArena().getState();
        if (!(state instanceof RunningState)) {
            return;
        }
        RunningState rs = (RunningState) state;
        
        Player pl = player.getPlayer();
        Location curr = pl.getLocation();
        
        if (this.blockLoc == null) {
            this.blockLoc = curr;
            return;
        }
        
        //Если игрок находится в одном блоке
        if ((curr.getBlockX() == this.blockLoc.getBlockX()) && (curr.getBlockY() == this.blockLoc.getBlockY()) && (curr.getBlockZ() == this.blockLoc.getBlockZ())) {
            handleSolidCount(player, curr, rs);
        }
        else {
            this.countBlock = placesCdTime;
            
            rs.undisguise(player);
            if (this.solid) {
                pl.sendMessage("Вы больше не твердый блок.");
                pl.playSound(player.getPlayer().getLocation(), Sound.ENTITY_BAT_AMBIENT, 100, 1.5f);
            }
            this.solid = false;
        }
        
        this.blockLoc = curr;
    }
    
    private void handleSolidCount(BHPlayer player, Location curr, RunningState rs) {
        if (this.cdBlock <= 0) {
            this.cdBlock = 20;
            Player pl = player.getPlayer();
            
            if (this.countBlock <= 0) {

                Block block = curr.getBlock();
                Material t = block.getType();
                if (!(t == Material.AIR || t == Material.WATER)) {
                    pl.sendMessage("Вы не можете стать твердым здесь!");
                    return;
                }
                
                rs.disguise(player, block);
                
                if (!this.solid) {
                    pl.sendMessage("Теперь вы твердый блок :)");
                    pl.playSound(player.getPlayer().getLocation(), Sound.ENTITY_BAT_AMBIENT, 100, 1.5f);
                }
                this.solid = true;
                return;
            }
            
            pl.sendMessage("До становления тведым блоком осталось " + this.countBlock);
            
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
                
                pl.sendMessage("Вам был выдан меч!");
                return;
            }

            
            this.countSword--;
        }
        this.cdSword--;
    }

}

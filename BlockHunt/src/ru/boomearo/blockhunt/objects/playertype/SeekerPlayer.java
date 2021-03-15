package ru.boomearo.blockhunt.objects.playertype;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.objects.BHPlayer;
import ru.boomearo.blockhunt.objects.ItemButton;
import ru.boomearo.blockhunt.utils.ExpFix;
import ru.boomearo.gamecontrol.GameControl;

public class SeekerPlayer implements IPlayerType {
    
    @Override
    public void preparePlayer(BHPlayer player) {
        if (Bukkit.isPrimaryThread()) {
            task(player);
        }
        else {
            Bukkit.getScheduler().runTask(BlockHunt.getInstance(), () -> {
                task(player);
            });
        }
    }
    
    private void task(BHPlayer player) {
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
        
        inv.setItem(EquipmentSlot.HEAD, new ItemStack(Material.IRON_HELMET, 1));
        inv.setItem(EquipmentSlot.CHEST, new ItemStack(Material.IRON_CHESTPLATE, 1));
        inv.setItem(EquipmentSlot.LEGS, new ItemStack(Material.IRON_LEGGINGS, 1));
        inv.setItem(EquipmentSlot.FEET, new ItemStack(Material.IRON_BOOTS, 1));
        
        inv.setItem(0, new ItemStack(Material.DIAMOND_SWORD, 1));
        
        inv.setHeldItemSlot(0);
        
        Location loc = player.getArena().getSeekersLocation();
        if (loc != null) {
            GameControl.getInstance().asyncTeleport(pl, loc);
        }
    }
    
}
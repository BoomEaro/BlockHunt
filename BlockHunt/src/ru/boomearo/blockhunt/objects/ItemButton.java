package ru.boomearo.blockhunt.objects;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.GameControlException;

public enum ItemButton {

    HiderSword(createHiderSwordButton(), 0, null),
    
    Leave(createLeaveButton(), 8, new ButtonClick() {

        @Override
        public void click(BHPlayer player) {
            try {
                GameControl.getInstance().getGameManager().leaveGame(player.getPlayer());
            } 
            catch (GameControlException e) {
                e.printStackTrace();
            }
        }
        
    });
    
    private ItemStack item;
    private int slot;
    private ButtonClick click;
    
    ItemButton(ItemStack item, int slot, ButtonClick click) {
        this.item = item;
        this.slot = slot;
        this.click = click;
    }
    
    public ItemStack getItem() {
        return this.item.clone();
    }
    
    public int getSlot() {
        return this.slot;
    }
    
    public ButtonClick getClick() {
        return this.click;
    }
    
    private static ItemStack createLeaveButton() {
        ItemStack item = new ItemStack(Material.MAGMA_CREAM, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cПокинуть игру §8[§cПКМ§8]");
        meta.setLore(Arrays.asList("§fКликните чтобы покинуть игру."));
        meta.addEnchant(Enchantment.DIG_SPEED, 1, true);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createHiderSwordButton() {
        ItemStack item = new ItemStack(Material.WOODEN_SWORD, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemButton getButtonByItem(ItemStack item) {
        for (ItemButton ib : values()) {
            if (ib.getItem().isSimilar(item)) {
                return ib;
            }
        }
        return null;
    }
    
    public static interface ButtonClick {
        public void click(BHPlayer player);
    }
}

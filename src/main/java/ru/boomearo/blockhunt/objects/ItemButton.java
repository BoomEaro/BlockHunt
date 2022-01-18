package ru.boomearo.blockhunt.objects;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.GameControlException;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.menu.MenuPage;
import ru.boomearo.blockhunt.menu.sessions.BHPlayerSession;
import ru.boomearo.menuinv.MenuInv;
import ru.boomearo.menuinv.exceptions.MenuInvException;

public enum ItemButton {

    HiderSword(0) {

        @Override
        public ItemStack getItem() {
            ItemStack item = new ItemStack(Material.WOODEN_SWORD, 1);
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void handleClick(BHPlayer player) {

        }

    },
    SeekerSword(0) {

        @Override
        public ItemStack getItem() {
            ItemStack item = new ItemStack(Material.DIAMOND_SWORD, 1);
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void handleClick(BHPlayer player) {

        }
    },

    BlockChoose(0) {
        @Override
        public ItemStack getItem() {
            ItemStack item = new ItemStack(Material.BOOK, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§9Выбрать блок §8[§9ПКМ§8]");
            meta.setLore(Arrays.asList("§fКликните чтобы выбрать блок."));
            meta.addEnchant(Enchantment.DIG_SPEED, 1, true);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void handleClick(BHPlayer player) {
            Player pl = player.getPlayer();

            if (!pl.hasPermission("blockhunt.blockchoose")) {
                return;
            }

            BHPlayerSession ps = new BHPlayerSession(player);

            try {
                MenuInv.getInstance().openMenu(MenuPage.CHOSEN.getPage(), pl, ps);
            }
            catch (MenuInvException e) {
                e.printStackTrace();
            }
        }
    },
    Leave(8) {

        @Override
        public ItemStack getItem() {
            ItemStack item = new ItemStack(Material.MAGMA_CREAM, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§cПокинуть игру §8[§cПКМ§8]");
            meta.setLore(Arrays.asList("§fКликните чтобы покинуть игру."));
            meta.addEnchant(Enchantment.DIG_SPEED, 1, true);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void handleClick(BHPlayer player) {
            try {
                GameControl.getInstance().getGameManager().leaveGame(player.getPlayer());
            }
            catch (GameControlException e) {
                e.printStackTrace();
            }
        }
    };

    private final int slot;

    ItemButton(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }

    public abstract ItemStack getItem();
    public abstract void handleClick(BHPlayer player);

    public static ItemButton getButtonByItem(ItemStack item) {
        for (ItemButton ib : values()) {
            if (ib.getItem().isSimilar(item)) {
                return ib;
            }
        }
        return null;
    }

}

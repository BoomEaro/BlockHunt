package ru.boomearo.blockhunt.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.nuop.sf.MenuMaker.Menu;
import com.gmail.nuop.sf.MenuMaker.inventory.InventoryData;
import com.gmail.nuop.sf.MenuMaker.inventory.InventoryStackControls;
import com.gmail.nuop.sf.MenuMaker.inventory.InventoryTemplate;
import com.gmail.nuop.sf.MenuMaker.inventory.MenuMakerException;
import com.gmail.nuop.sf.MenuMaker.inventory.PageMaker;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.menu.icons.BlockIcon;
import ru.boomearo.blockhunt.menu.sessions.PlayerSession;

public final class MenuManager {

    private InventoryTemplate mainBlockChoose = null;

    public MenuManager() {
        try {
            initMainBlockChooseMenu();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void initMainBlockChooseMenu() throws MenuMakerException {
        InventoryTemplate form = Menu.createTemplate(5);

        PageMaker pm = form.createPage("main");

        pm.addStackControls("blocks", 1, 1, 7, 3);

        this.mainBlockChoose = form;
    }


    private static void sortItemsBlocks(InventoryStackControls isc, PlayerSession session) {
        int scroll = isc.getThisScroll();
        isc.eraseAll();

        for (Material mat : session.getBHPlayer().getArena().getAllHideBlocks()) {
            ItemStack copy = new ItemStack(mat, 1);

            ItemMeta im = copy.getItemMeta();
            im.addItemFlags(ItemFlag.values());

            List<String> tmp = new ArrayList<String>();

            tmp.add("§f► Кликните чтобы стать этим блоком в следующей игре.");

            im.setLore(tmp);

            copy.setItemMeta(im);
            
            isc.add(copy, new BlockIcon(mat)); 
        }

        isc.setScroll(scroll);
    }


    public void openPage(Player player, PlayerSession session, MenuPage page) {
        Bukkit.getScheduler().runTask(BlockHunt.getInstance(), () -> {
            try {
                session.setWhere(page);
                switch (page) {
                    case MainBlockChoose: {

                        InventoryData newData = this.mainBlockChoose.createInventory(BlockHunt.getInstance(), "§0§lВыберите блок", player, session);

                        sortItemsBlocks(newData.getActualPage().getStackControls("blocks"), session);
                        
                        newData.updateInventory();
                        newData.lockPlayerInventory();
                        
                        break;
                    }
                }
            }
            catch (Throwable t) {
                player.closeInventory();
                t.printStackTrace();
            }
        });
    }

}

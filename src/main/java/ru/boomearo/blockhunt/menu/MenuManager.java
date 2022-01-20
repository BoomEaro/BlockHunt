package ru.boomearo.blockhunt.menu;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.menu.icons.BlockIcon;
import ru.boomearo.blockhunt.menu.sessions.BHPlayerSession;
import ru.boomearo.menuinv.MenuInv;
import ru.boomearo.menuinv.api.*;
import ru.boomearo.menuinv.api.session.InventorySession;
import ru.boomearo.menuinv.exceptions.MenuInvException;

public final class MenuManager {

    public static void initMenu(BlockHunt blockHunt) throws MenuInvException {
        PluginTemplatePages pages = MenuInv.getInstance().registerPages(blockHunt);

        initChosenMenu(pages);
    }

    private static void initChosenMenu(PluginTemplatePages pages) throws MenuInvException {
        TemplatePage page = pages.createTemplatePage(MenuPage.CHOSEN.name(), InvType.CHEST_9X6, (inventoryPage) -> "§0§lВыберите блок");

        page.addPagedItems("items", 1, 1, 7, 3, () -> new FramedIconsHandler() {

            @Override
            public List<IconHandler> onUpdate(InventoryPage inventoryPage, Player player) {
                InventorySession session = inventoryPage.getSession();
                if (!(session instanceof BHPlayerSession bhPlayerSession)) {
                    return null;
                }

                List<IconHandler> icons = new ArrayList<>();
                for (Material mat : bhPlayerSession.getBHPlayer().getArena().getAllHideBlocks()) {
                    icons.add(new BlockIcon(mat));
                }

                return icons;
            }

        }, true);
    }

}

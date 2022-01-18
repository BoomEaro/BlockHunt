package ru.boomearo.blockhunt.menu.icons;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.menu.sessions.BHPlayerSession;
import ru.boomearo.langhelper.LangHelper;
import ru.boomearo.langhelper.versions.LangType;
import ru.boomearo.menuinv.api.IconHandler;
import ru.boomearo.menuinv.api.InventoryPage;
import ru.boomearo.menuinv.api.session.InventorySession;

import java.util.ArrayList;
import java.util.List;

public class BlockIcon extends IconHandler {

    private final Material mat;

    public BlockIcon(Material mat) {
        this.mat = mat;
    }

    @Override
    public void onClick(InventoryPage inventoryPage, Player player, ClickType clickType) {
        InventorySession session = inventoryPage.getSession();
        if (!(session instanceof BHPlayerSession bhPlayerSession)) {
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 15, 1f);

        bhPlayerSession.getBHPlayer().setChosenBlock(this.mat);
        player.sendMessage(BlockHuntManager.prefix + "Вы выбрали блок " + BlockHuntManager.variableColor + LangHelper.getInstance().getItemTranslate(new ItemStack(this.mat, 1), LangType.RU_RU));

        inventoryPage.close();
    }

    @Override
    public ItemStack onUpdate(InventoryPage inventoryPage, Player player) {
        ItemStack item = new ItemStack(this.mat, 1);

        ItemMeta im = item.getItemMeta();
        im.addItemFlags(ItemFlag.values());

        List<String> tmp = new ArrayList<>();

        tmp.add("§f► Кликните чтобы стать этим блоком в следующей игре.");

        im.setLore(tmp);

        item.setItemMeta(im);

        return item;
    }
}

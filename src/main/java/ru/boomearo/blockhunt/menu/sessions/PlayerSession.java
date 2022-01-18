package ru.boomearo.blockhunt.menu.sessions;

import org.bukkit.entity.Player;

import com.gmail.nuop.sf.MenuMaker.inventory.InventorySession;

import ru.boomearo.blockhunt.menu.MenuPage;
import ru.boomearo.blockhunt.objects.BHPlayer;

public class PlayerSession implements InventorySession {

    private final Player player;

    private BHPlayer bhPlayer;

    private MenuPage where = MenuPage.MainBlockChoose;
    private MenuPage last = MenuPage.MainBlockChoose;

    public PlayerSession(Player player, BHPlayer bhPlayer, MenuPage page) {
        this.player = player;
        this.bhPlayer = bhPlayer;

        this.where = page;
        this.last = page;
    }

    public Player getPlayer() {
        return this.player;
    }

    public BHPlayer getBHPlayer() {
        return this.bhPlayer;
    }

    public MenuPage getWhere() {
        return this.where;
    }

    public MenuPage getLast() {
        return this.last;
    }

    public void setWhere(MenuPage where) {
        this.last = this.where;
        this.where = where;
    }

}

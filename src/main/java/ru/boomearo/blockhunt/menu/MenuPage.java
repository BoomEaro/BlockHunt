package ru.boomearo.blockhunt.menu;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.menuinv.api.PageData;

public enum MenuPage {

    CHOSEN(new PageData(BlockHunt.getInstance(), "CHOSEN"));

    private final PageData page;

    MenuPage(PageData page) {
        this.page = page;
    }

    public PageData getPage() {
        return this.page;
    }
}

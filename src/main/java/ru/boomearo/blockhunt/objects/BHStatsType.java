package ru.boomearo.blockhunt.objects;

import org.bukkit.Material;
import ru.boomearo.gamecontrol.objects.statistics.IStatsType;

public enum BHStatsType implements IStatsType {

    SeekersWin("Побед Охотником", "seekersWin", Material.SOUL_LANTERN),
    HidersWin("Побед Хайдером", "hidersWin", Material.BEACON),
    SeekersKills("Убито Охотников", "seekersKills", Material.DIAMOND_SWORD),
    HidersKills("Убито Хайдеров", "hidersKills", Material.CRAFTING_TABLE);

    private final String name;
    private final String dbName;
    private final Material icon;

    BHStatsType(String name, String dbName, Material icon) {
        this.name = name;
        this.dbName = dbName;
        this.icon = icon;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getTableName() {
        return this.dbName;
    }

    @Override
    public Material getIcon() {
        return this.icon;
    }

}

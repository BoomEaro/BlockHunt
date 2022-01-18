package ru.boomearo.blockhunt.managers;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ru.boomearo.blockhunt.database.Sql;
import ru.boomearo.blockhunt.objects.statistics.BHStatsData;
import ru.boomearo.blockhunt.objects.statistics.BHStatsType;
import ru.boomearo.gamecontrol.objects.statistics.IStatisticsManager;
import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;

public class BlockHuntStatistics implements IStatisticsManager {

    private final ConcurrentMap<BHStatsType, BHStatsData> stats = new ConcurrentHashMap<>();
    
    public BlockHuntStatistics() {
        for (BHStatsType type : BHStatsType.values()) {
            this.stats.put(type, new BHStatsData(type));
        }
    }
    
    @Override
    public BHStatsData getStatsData(String name) {
        BHStatsType type = null;
        try {
            type = BHStatsType.valueOf(name);
        }
        catch (Exception ignored) {}
        if (type == null) {
            return null;
        }
        
        return this.stats.get(type);
    }

    @Override
    public Collection<BHStatsData> getAllStatsData() {
        return this.stats.values();
    }
    
    public BHStatsData getStatsData(BHStatsType type) {
        return this.stats.get(type);
    }
    
    public void addStats(BHStatsType type, String name) {
        BHStatsData data = this.stats.get(type);
        StatsPlayer sp = data.getStatsPlayer(name);
        if (sp == null) {
            StatsPlayer newSp = new StatsPlayer(name, 1);
            data.addStatsPlayer(newSp);
            Sql.getInstance().putStatsData(type, name, 1);
            return;
        }
        sp.setValue(sp.getValue() + 1);
        Sql.getInstance().updateStatsData(type, name, sp.getValue());
    }

}

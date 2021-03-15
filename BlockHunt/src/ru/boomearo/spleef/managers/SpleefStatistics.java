package ru.boomearo.spleef.managers;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ru.boomearo.gamecontrol.objects.statistics.IStatisticsManager;
import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;
import ru.boomearo.spleef.database.runnable.PutStats;
import ru.boomearo.spleef.database.runnable.UpdateStats;
import ru.boomearo.spleef.objects.statistics.SpleefStatsData;
import ru.boomearo.spleef.objects.statistics.SpleefStatsType;

public class SpleefStatistics implements IStatisticsManager {

    private final ConcurrentMap<SpleefStatsType, SpleefStatsData> stats = new ConcurrentHashMap<SpleefStatsType, SpleefStatsData>();
    
    public SpleefStatistics() {
        for (SpleefStatsType type : SpleefStatsType.values()) {
            this.stats.put(type, new SpleefStatsData(type));
        }
    }
    
    @Override
    public SpleefStatsData getStatsData(String name) {
        SpleefStatsType type = null;
        try {
            type = SpleefStatsType.valueOf(name);
        }
        catch (Exception e) {}
        if (type == null) {
            return null;
        }
        
        return this.stats.get(type);
    }

    @Override
    public Collection<SpleefStatsData> getAllStatsData() {
        return this.stats.values();
    }
    
    public SpleefStatsData getStatsData(SpleefStatsType type) {
        return this.stats.get(type);
    }
    
    public void addStats(SpleefStatsType type, String name) {
        SpleefStatsData data = this.stats.get(type);
        StatsPlayer sp = data.getStatsPlayer(name);
        if (sp == null) {
            StatsPlayer newSp = new StatsPlayer(name, 1);
            data.addStatsPlayer(newSp);
            new PutStats(type, newSp);
            return;
        }
        sp.setValue(sp.getValue() + 1);
        new UpdateStats(type, sp);
    }

}

package ru.boomearo.blockhunt.objects.statistics;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ru.boomearo.gamecontrol.objects.statistics.IStatsData;
import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;

public class BHStatsData implements IStatsData {

    private final BHStatsType type;

    private final ConcurrentMap<String, StatsPlayer> players = new ConcurrentHashMap<>();

    public BHStatsData(BHStatsType type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return this.type.name();
    }

    @Override
    public StatsPlayer getStatsPlayer(String name) {
        return this.players.get(name);
    }

    @Override
    public Collection<StatsPlayer> getAllStatsPlayer() {
        return this.players.values();
    }

    public BHStatsType getType() {
        return this.type;
    }

    public void addStatsPlayer(StatsPlayer data) {
        this.players.put(data.getName(), data);
    }
}

package ru.boomearo.blockhunt.objects.statistics;

public enum BHStatsType {

    SeekersWin("Побед Сикером", "seekersWin"),
    HidersWin("Побед Хайдером", "hidersWin"),
    SeekersKills("Убито Сикеров", "seekersKills"),
    HidersKills("Убито Хайдеров", "hidersKills");
    
    private final String name;
    private final String dbName;
    
    BHStatsType(String name, String dbName) {
        this.name = name;
        this.dbName = dbName;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getDBName() {
        return this.dbName;
    }
    
}

package ru.boomearo.spleef.objects.statistics;

public enum SpleefStatsType {

    Wins("Побед", "wins"),
    Defeat("Поражений", "defeats");
    
    private final String name;
    private final String dbName;
    
    SpleefStatsType(String name, String dbName) {
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

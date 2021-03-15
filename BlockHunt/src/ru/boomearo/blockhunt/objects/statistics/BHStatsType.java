package ru.boomearo.blockhunt.objects.statistics;

public enum BHStatsType {

    Wins("Побед", "wins"),
    Defeat("Поражений", "defeats");
    
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

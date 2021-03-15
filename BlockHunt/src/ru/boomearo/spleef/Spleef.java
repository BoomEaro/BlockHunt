package ru.boomearo.spleef;

import java.io.File;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.spawn.EssentialsSpawn;

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.objects.states.IGameState;
import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;
import ru.boomearo.spleef.commands.spleef.CmdExecutorSpleef;
import ru.boomearo.spleef.database.Sql;
import ru.boomearo.spleef.database.sections.SectionStats;
import ru.boomearo.spleef.listeners.ArenaListener;
import ru.boomearo.spleef.listeners.PlayerButtonListener;
import ru.boomearo.spleef.listeners.PlayerListener;
import ru.boomearo.spleef.listeners.SpectatorListener;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefArena;
import ru.boomearo.spleef.objects.SpleefTeam;
import ru.boomearo.spleef.objects.region.CuboidRegion;
import ru.boomearo.spleef.objects.state.RegenState;
import ru.boomearo.spleef.objects.statistics.SpleefStatsData;
import ru.boomearo.spleef.objects.statistics.SpleefStatsType;
import ru.boomearo.spleef.runnable.ArenasRunnable;

public class Spleef extends JavaPlugin {
    
    private SpleefManager arenaManager = null;
    
    private ArenasRunnable pmr = null;

    private EssentialsSpawn essSpawn = null;
    
    private static Spleef instance = null;

    public void onEnable() {
        instance = this;
        
        this.essSpawn = (EssentialsSpawn) Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
        
        ConfigurationSerialization.registerClass(CuboidRegion.class);
        ConfigurationSerialization.registerClass(SpleefArena.class);
        ConfigurationSerialization.registerClass(SpleefTeam.class);
        
        File configFile = new File(getDataFolder() + File.separator + "config.yml");
        if(!configFile.exists()) {
            getLogger().info("Конфиг не найден, создаю новый...");
            saveDefaultConfig();
        }
        
        if (this.arenaManager == null) {
            this.arenaManager = new SpleefManager();
        }
        
        loadDataBase();
        loadDataFromDatabase();
        
        try {
            GameControl.getInstance().getGameManager().registerGame(this.getClass(), this.arenaManager);
        } 
        catch (ConsoleGameException e) {
            e.printStackTrace();
        }
        
        getCommand("spleef").setExecutor(new CmdExecutorSpleef());
        
        getServer().getPluginManager().registerEvents(new ArenaListener(), this);
        
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerButtonListener(), this);
        
        getServer().getPluginManager().registerEvents(new SpectatorListener(), this);
        
        if (this.pmr == null) {
            this.pmr = new ArenasRunnable();
        }
        
        getLogger().info("Плагин успешно запущен.");
    }
    
    
    public void onDisable() {
        try {
            getLogger().info("Отключаюсь от базы данных");
            Sql.getInstance().Disconnect();
            getLogger().info("Успешно отключился от базы данных");
        } 
        catch (SQLException e) {
            e.printStackTrace();
            getLogger().info("Не удалось отключиться от базы данных...");
        }
        
        try {
            GameControl.getInstance().getGameManager().unregisterGame(this.getClass());
        } 
        catch (ConsoleGameException e) {
            e.printStackTrace();
        }

        for (SpleefArena ar : this.arenaManager.getAllArenas()) {
            IGameState state = ar.getState();
            //Если выключение сервера застал в момент регенерации, то ничего не делаем
            if (state instanceof RegenState) {
                continue;
            }
            ar.regen();
        }
        
        ConfigurationSerialization.unregisterClass(CuboidRegion.class);
        ConfigurationSerialization.unregisterClass(SpleefArena.class);
        ConfigurationSerialization.unregisterClass(SpleefTeam.class);
        
        getLogger().info("Плагин успешно выключен.");
    }
    
    public SpleefManager getSpleefManager() {
        return this.arenaManager;
    }
    
    public EssentialsSpawn getEssentialsSpawn() {
        return this.essSpawn;
    }
    
    public File getSchematicDir() {
        return new File(this.getDataFolder(), File.separator + "schematics" + File.separator);
    }
    
    private void loadDataBase() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir(); 

        }
        try {
            for (SpleefStatsType type : SpleefStatsType.values()) {
                Sql.getInstance().createNewDatabaseStatsData(type);
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDataFromDatabase() {
        try {
            for (SpleefStatsType type : SpleefStatsType.values()) {
                SpleefStatsData data = this.arenaManager.getStatisticManager().getStatsData(type);
                for (SectionStats stats : Sql.getInstance().getAllStatsData(type)) {
                    data.addStatsPlayer(new StatsPlayer(stats.name, stats.value));
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static Spleef getInstance() { 
        return instance;
    }

}

package ru.boomearo.spleef.database.runnable;

import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.database.Sql;
import ru.boomearo.spleef.objects.statistics.SpleefStatsType;

public class UpdateStats extends BukkitRunnable {

    private final SpleefStatsType type;
    private final StatsPlayer player;
    
    public UpdateStats(SpleefStatsType type, StatsPlayer player) {
        this.player = player;
        this.type = type;
        runnable();
    }
    
	
	private void runnable() {
		this.runTaskAsynchronously(Spleef.getInstance());
	}
	
	@Override
	public void run() {
		try {
			Sql.getInstance().updateStatsData(this.type, this.player.getName(), this.player.getValue());
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}

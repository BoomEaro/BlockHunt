package ru.boomearo.blockhunt.database.runnable;

import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.database.Sql;
import ru.boomearo.blockhunt.objects.statistics.BHStatsType;
import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;

public class PutStats extends BukkitRunnable {

    private final BHStatsType type;
	private final StatsPlayer player;
	
	public PutStats(BHStatsType type, StatsPlayer player) {
		this.player = player;
		this.type = type;
		runnable();
	}
	
	private void runnable() {
		this.runTaskAsynchronously(BlockHunt.getInstance());
	}
	
	@Override
	public void run() {
		try {
			Sql.getInstance().putStatsData(this.type, this.player.getName(), this.player.getValue());
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}

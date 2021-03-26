package ru.boomearo.blockhunt;

import java.io.File;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

import ru.boomearo.blockhunt.commands.blockhunt.CmdExecutorBlockHunt;
import ru.boomearo.blockhunt.database.Sql;
import ru.boomearo.blockhunt.database.sections.SectionStats;
import ru.boomearo.blockhunt.listeners.bukkit.ArenaListener;
import ru.boomearo.blockhunt.listeners.bukkit.PlayerButtonListener;
import ru.boomearo.blockhunt.listeners.bukkit.PlayerListener;
import ru.boomearo.blockhunt.listeners.packet.PacketBlockFormAdapter;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.menu.MenuManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.statistics.BHStatsData;
import ru.boomearo.blockhunt.objects.statistics.BHStatsType;
import ru.boomearo.blockhunt.runnable.ArenasRunnable;
import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;

public class BlockHunt extends JavaPlugin {

    private BlockHuntManager arenaManager = null;
    
    private MenuManager menu = null;

    private ArenasRunnable pmr = null;

    private static BlockHunt instance = null;

    public void onEnable() {
        instance = this;

        ConfigurationSerialization.registerClass(BHArena.class);

        File configFile = new File(getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            getLogger().info("Конфиг не найден, создаю новый...");
            saveDefaultConfig();
        }

        if (this.arenaManager == null) {
            this.arenaManager = new BlockHuntManager();
            
            //После загрузки сервера запускаем задачу на перманентную подгрузку чанков
            Bukkit.getScheduler().runTask(this, () -> {
                for (BHArena arena : this.arenaManager.getAllArenas()) {
                    arena.forceLoadChunksToMemory();
                }
            });
        }
        
        if (this.menu == null) {
            this.menu = new MenuManager();
        }

        loadDataBase();
        loadDataFromDatabase();

        try {
            GameControl.getInstance().getGameManager().registerGame(this.getClass(), this.arenaManager);
        } 
        catch (ConsoleGameException e) {
            e.printStackTrace();
        }

        getCommand("blockhunt").setExecutor(new CmdExecutorBlockHunt());

        getServer().getPluginManager().registerEvents(new ArenaListener(), this);

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerButtonListener(), this);
        
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketBlockFormAdapter());

        if (this.pmr == null) {
            this.pmr = new ArenasRunnable();
        }

        getLogger().info("Плагин успешно запущен.");
    }

    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
        
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

        ConfigurationSerialization.unregisterClass(BHArena.class);

        getLogger().info("Плагин успешно выключен.");
    }

    public BlockHuntManager getBlockHuntManager() {
        return this.arenaManager;
    }

    public MenuManager getMenuManager() {
        return this.menu;
    }

    private void loadDataBase() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        try {
            for (BHStatsType type : BHStatsType.values()) {
                Sql.getInstance().createNewDatabaseStatsData(type);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDataFromDatabase() {
        try {
            for (BHStatsType type : BHStatsType.values()) {
                BHStatsData data = this.arenaManager.getStatisticManager()
                        .getStatsData(type);
                for (SectionStats stats : Sql.getInstance()
                        .getAllStatsData(type)) {
                    data.addStatsPlayer(
                            new StatsPlayer(stats.name, stats.value));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static BlockHunt getInstance() {
        return instance;
    }

    /*public static void test() {
        for (Player player : arena.playersInArena) {
            if (!arena.seekers.contains(player)) {
                Location pLoc = player.getLocation();
                Location moveLoc = W.moveLoc.get(player);
                ItemStack block = player.getInventory().getItem(8);

                if (block == null) {
                    if (W.pBlock.get(player) != null) {
                        block = W.pBlock.get(player);
                        player.getInventory().setItem(8, block);
                        player.updateInventory();
                    }
                }

                if (moveLoc != null) {
                    if (moveLoc.getX() == pLoc.getX()
                            && moveLoc.getY() == pLoc.getY()
                            && moveLoc.getZ() == pLoc.getZ()) {
                        if (block.getAmount() > 1) {
                            block.setAmount(block.getAmount() - 1);
                        } 
                        else {
                            Block pBlock = player.getLocation().getBlock();
                            if (pBlock.getType().equals(Material.AIR)
                                    || pBlock.getType().equals(Material.WATER)
                                    || pBlock.getType().equals(Material.STATIONARY_WATER)) {
                                if (pBlock.getType().equals(Material.WATER)
                                        || pBlock.getType().equals(Material.STATIONARY_WATER)) {
                                    W.hiddenLocWater.put(player, true);
                                } 
                                else {
                                    W.hiddenLocWater.put(player, false);
                                }
                                if (DisguiseAPI.isDisguised(player)) {
                                    DisguiseAPI.undisguiseToAll(player);
                                    for (Player pl : Bukkit
                                            .getOnlinePlayers()) {
                                        if (!pl.equals(player)) {
                                            pl.hidePlayer(player);
                                            pl.sendBlockChange(
                                                    pBlock.getLocation(),
                                                    block.getType(),
                                                    (byte) block
                                                    .getDurability());
                                        }
                                    }

                                    block.addUnsafeEnchantment(
                                            Enchantment.DURABILITY, 10);
                                    player.playSound(pLoc,
                                            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                                            1, 1);
                                    W.hiddenLoc.put(player, moveLoc);
                                    if (block.getDurability() != 0) {
                                        MessageM.sendFMessage(player,
                                                ConfigC.normal_ingameNowSolid,
                                                "block-" + block.getType()
                                                .name()
                                                .replaceAll("_", "")
                                                .replaceAll("BLOCK", "")
                                                .toLowerCase() + ":"
                                                + block.getDurability());
                                    } else {
                                        MessageM.sendFMessage(player,
                                                ConfigC.normal_ingameNowSolid,
                                                "block-" + block.getType()
                                                .name()
                                                .replaceAll("_", "")
                                                .replaceAll("BLOCK", "")
                                                .toLowerCase());
                                    }
                                }
                                for (Player pl : Bukkit.getOnlinePlayers()) {
                                    if (!pl.equals(player)) {
                                        pl.hidePlayer(player);
                                        pl.sendBlockChange(pBlock.getLocation(),
                                                block.getType(),
                                                (byte) block.getDurability());
                                    }
                                }
                            } else {
                                MessageM.sendFMessage(player,
                                        ConfigC.warning_ingameNoSolidPlace);
                            }
                        }
                    } else {
                        block.setAmount(5);
                        if (!DisguiseAPI.isDisguised(player)) {
                            SolidBlockHandler.makePlayerUnsolid(player);
                        }
                    }
                }
            }
        }
    }*/
}

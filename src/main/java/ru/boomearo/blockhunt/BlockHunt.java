package ru.boomearo.blockhunt;

import java.io.File;
import java.sql.SQLException;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

import ru.boomearo.blockhunt.commands.blockhunt.CmdExecutorBlockHunt;
import ru.boomearo.blockhunt.database.Sql;
import ru.boomearo.blockhunt.database.sections.SectionStats;
import ru.boomearo.blockhunt.listeners.bukkit.PlayerButtonListener;
import ru.boomearo.blockhunt.listeners.bukkit.PlayerListener;
import ru.boomearo.blockhunt.listeners.packet.PacketBlockFormAdapter;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.menu.MenuManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.statistics.BHStatsData;
import ru.boomearo.blockhunt.objects.statistics.BHStatsType;
import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.objects.statistics.StatsPlayer;
import ru.boomearo.menuinv.MenuInv;
import ru.boomearo.menuinv.exceptions.MenuInvException;

public class BlockHunt extends JavaPlugin {

    private BlockHuntManager arenaManager = null;

    private static BlockHunt instance = null;

    @Override
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
        }

        try {
            MenuManager.initMenu(this);
        }
        catch (MenuInvException e) {
            e.printStackTrace();
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

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerButtonListener(), this);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketBlockFormAdapter());

        getLogger().info("Плагин успешно запущен.");
    }

    @Override
    public void onDisable() {
        try {
            MenuInv.getInstance().unregisterPages(this);
        }
        catch (MenuInvException e) {
            e.printStackTrace();
        }

        ProtocolLibrary.getProtocolManager().removePacketListeners(this);

        try {
            getLogger().info("Отключаюсь от базы данных");
            Sql.getInstance().disconnect();
            getLogger().info("Успешно отключился от базы данных");
        }
        catch (Exception e) {
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

    private void loadDataBase() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        try {
            Sql.initSql();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDataFromDatabase() {
        try {
            for (BHStatsType type : BHStatsType.values()) {
                BHStatsData data = this.arenaManager.getStatisticManager().getStatsData(type);
                for (SectionStats stats : Sql.getInstance().getAllStatsData(type).get()) {
                    data.addStatsPlayer(new StatsPlayer(stats.name, stats.value));
                }
            }
        }
        catch (Exception e) {
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

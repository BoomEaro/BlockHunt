package ru.boomearo.blockhunt.commands.blockhunt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.commands.CmdInfo;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.exceptions.PlayerGameException;
import ru.boomearo.gamecontrol.objects.region.CuboidRegion;

public class BlockHuntUse {


    @CmdInfo(name = "createarena", description = "Создать арену с указанным названием.", usage = "/blockhunt createarena <название>", permission = "blockhunt.admin")
    public boolean createarena(CommandSender cs, String[] args) {
        if (!(cs instanceof Player)) {
            cs.sendMessage("Данная команда только для игроков.");
            return true;
        }
        if (args.length < 1 || args.length > 1) {
            return false;
        }
        String arena = args[0];
        Player pl = (Player) cs;

        BukkitPlayer bPlayer = BukkitAdapter.adapt(pl);
        LocalSession ls = WorldEdit.getInstance().getSessionManager().get(bPlayer);
        if (ls == null) {
            pl.sendMessage(BlockHuntManager.prefix + "Выделите регион!");
            return true;
        }
        Region re = null;
        try {
            re = ls.getSelection(ls.getSelectionWorld());
        }
        catch (IncompleteRegionException e) {
        }
        if (re == null) {
            pl.sendMessage(BlockHuntManager.prefix + "Выделите регион!");
            return true;
        }

        try {
            List<Material> bl = new ArrayList<Material>();
            bl.add(Material.STONE);

            BHArena newArena = new BHArena(arena, pl.getWorld(), Material.STONE, 2, 15, 300, new CuboidRegion(re.getMaximumPoint(), re.getMinimumPoint(), pl.getWorld()), null, null, null, null, bl);

            BlockHuntManager am = BlockHunt.getInstance().getBlockHuntManager();
            am.addArena(newArena);

            am.saveArenas();

            pl.sendMessage(BlockHuntManager.prefix + "Арена '§e" + arena + "§b' успешно создана!");
        }
        catch (Exception e) {
            pl.sendMessage(e.getMessage());
        }

        return true;
    }

    @CmdInfo(name = "setpoint", description = "Установить указанную точку указанной арене.", usage = "/blockhunt setpoint <lobby/seeker/hider> <арена>", permission = "blockhunt.admin")
    public boolean setspawnpoint(CommandSender cs, String[] args) {
        if (!(cs instanceof Player)) {
            cs.sendMessage("Данная команда только для игроков.");
            return true;
        }
        if (args.length < 2 || args.length > 2) {
            return false;
        }
        String arena = args[1];
        Player pl = (Player) cs;

        BlockHuntManager trm = BlockHunt.getInstance().getBlockHuntManager();
        BHArena ar = trm.getGameArena(arena);
        if (ar == null) {
            cs.sendMessage(BlockHuntManager.prefix + "Арена '§e" + arena + "§b' не найдена!");
            return true;
        }


        String s = args[0].toLowerCase();

        switch (s) {
            case "lobby": {
                BukkitPlayer bPlayer = BukkitAdapter.adapt(pl);
                LocalSession ls = WorldEdit.getInstance().getSessionManager().get(bPlayer);
                if (ls == null) {
                    pl.sendMessage(BlockHuntManager.prefix + "Выделите регион!");
                    return true;
                }
                Region re = null;
                try {
                    re = ls.getSelection(ls.getSelectionWorld());
                }
                catch (IncompleteRegionException e) {
                }
                if (re == null) {
                    pl.sendMessage(BlockHuntManager.prefix + "Выделите регион!");
                    return true;
                }

                ar.setLobbyLocation(GameControl.normalizeRotation(pl.getLocation()));

                ar.setLobbyRegion(new CuboidRegion(re.getMaximumPoint(), re.getMinimumPoint(), pl.getWorld()));
                break;
            }
            case "seeker": {
                ar.setSeekersLocation(GameControl.normalizeRotation(pl.getLocation()));
                break;
            }
            case "hider": {
                ar.setHidersLocation(GameControl.normalizeRotation(pl.getLocation()));
                break;
            }
            default: {
                cs.sendMessage(BlockHuntManager.prefix + "Аргумент должен быть §elobby, seeker или hider§b.");
                return true;
            }
        }

        trm.saveArenas();

        cs.sendMessage(BlockHuntManager.prefix + "Точка §e" + s + " §bуспешно установлена в арене '§e" + arena + "§b'");

        return true;
    }

    @CmdInfo(name = "join", description = "Присоединиться к указанной арене.", usage = "/blockhunt join <арена>", permission = "")
    public boolean join(CommandSender cs, String[] args) {
        if (!(cs instanceof Player)) {
            cs.sendMessage("Данная команда только для игроков.");
            return true;
        }
        if (args.length < 1 || args.length > 1) {
            return false;
        }
        String arena = args[0];
        Player pl = (Player) cs;

        try {
            GameControl.getInstance().getGameManager().joinGame(pl, BlockHunt.class, arena);
        }
        catch (PlayerGameException e) {
            pl.sendMessage(BlockHuntManager.prefix + "§cОшибка: " + BlockHuntManager.mainColor + e.getMessage());
        }
        catch (ConsoleGameException e) {
            e.printStackTrace();
            pl.sendMessage(BlockHuntManager.prefix + "§cПроизошла ошибка, сообщите администрации!");
        }
        return true;
    }

    @CmdInfo(name = "leave", description = "Покинуть игру.", usage = "/blockhunt leave", permission = "")
    public boolean leave(CommandSender cs, String[] args) {
        if (!(cs instanceof Player)) {
            cs.sendMessage("Данная команда только для игроков.");
            return true;
        }
        if (args.length < 0 || args.length > 0) {
            return false;
        }
        Player pl = (Player) cs;

        try {
            GameControl.getInstance().getGameManager().leaveGame(pl);
        }
        catch (PlayerGameException e) {
            pl.sendMessage(BlockHuntManager.prefix + "§cОшибка: " + BlockHuntManager.mainColor + e.getMessage());
        }
        catch (ConsoleGameException e) {
            e.printStackTrace();
            pl.sendMessage(BlockHuntManager.prefix + "§cПроизошла ошибка, сообщите администрации!");
        }
        return true;
    }

    @CmdInfo(name = "list", description = "Показать список всех доступных арен.", usage = "/blockhunt list", permission = "")
    public boolean list(CommandSender cs, String[] args) {
        if (args.length < 0 || args.length > 0) {
            return false;
        }

        Collection<BHArena> arenas = BlockHunt.getInstance().getBlockHuntManager().getAllArenas();
        if (arenas.isEmpty()) {
            cs.sendMessage(BlockHuntManager.prefix + "Арены еще не созданы!");
            return true;
        }
        final String sep = BlockHuntManager.prefix + "§8============================";
        cs.sendMessage(sep);
        for (BHArena arena : arenas) {
            cs.sendMessage(BlockHuntManager.prefix + "Арена: '" + BlockHuntManager.variableColor + arena.getName() + BlockHuntManager.mainColor + "'. Статус: " + arena.getState().getName() + BlockHuntManager.mainColor + ". Игроков: " + BlockHuntManager.getRemainPlayersArena(arena, null));
        }
        cs.sendMessage(sep);

        return true;
    }
}

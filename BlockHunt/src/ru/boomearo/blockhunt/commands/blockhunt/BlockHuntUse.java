package ru.boomearo.blockhunt.commands.blockhunt;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.commands.CmdInfo;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.blockhunt.objects.SpleefTeam;
import ru.boomearo.blockhunt.objects.region.CuboidRegion;
import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.exceptions.PlayerGameException;

public class BlockHuntUse {


    @CmdInfo(name = "createarena", description = "Создать арену с указанным названием.", usage = "/spleef createarena <название>", permission = "spleef.admin")
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
        Region re = ls.getSelection(ls.getSelectionWorld());
        if (re == null) {
            pl.sendMessage(BlockHuntManager.prefix + "Выделите регион!");
            return true;
        }

        ConcurrentMap<Integer, SpleefTeam> teams = new ConcurrentHashMap<Integer, SpleefTeam>();
        
        int maxPlayers = 15;
        
        for (int i = 1; i <= maxPlayers; i++) {
            teams.put(i, new SpleefTeam(i, null));
        }
        
        try {
            BHArena newArena = new BHArena(arena, 2, maxPlayers, 300, pl.getWorld(), new CuboidRegion(re.getMaximumPoint(), re.getMinimumPoint(), pl.getWorld()), teams, pl.getLocation(), null);
            
            BlockHuntManager am = BlockHunt.getInstance().getBlockHuntManager();
            am.addArena(newArena);

            am.saveArenas();

            pl.sendMessage(BlockHuntManager.prefix + "Арена '§b" + arena + "§7' успешно создана!");
        }
        catch (Exception e) {
            pl.sendMessage(e.getMessage());
        }
        
        return true;
    }
    
    @CmdInfo(name = "setspawnpoint", description = "Установить точку спавна в указанной арене указанной команде.", usage = "/spleef setspawnpoint <арена> <ид>", permission = "spleef.admin")
    public boolean setspawnpoint(CommandSender cs, String[] args) {
        if (!(cs instanceof Player)) {
            cs.sendMessage("Данная команда только для игроков.");
            return true;
        }
        if (args.length < 2 || args.length > 2) {
            return false;
        }
        String arena = args[0];
        Player pl = (Player) cs;

        BlockHuntManager trm = BlockHunt.getInstance().getBlockHuntManager();
        BHArena ar = trm.getGameArena(arena);
        if (ar == null) {
            cs.sendMessage(BlockHuntManager.prefix + "Арена '§b" + arena + "§7' не найдена!");
            return true;
        }
        
        Integer id = null;
        try {
            id = Integer.parseInt(args[1]);
        }
        catch (Exception e) {}
        if (id == null) {
            cs.sendMessage(BlockHuntManager.prefix + "Аргумент должен быть цифрой!");
            return true;
        }
        
        SpleefTeam team = ar.getTeamById(id);
        if (team == null) {
            cs.sendMessage(BlockHuntManager.prefix + "Команда §b" + id + " §7не найдена!");
            return true;
        }
        
        team.setSpawnPoint(pl.getLocation().clone());
        
        trm.saveArenas();
        
        cs.sendMessage(BlockHuntManager.prefix + "Спавн поинт §b" + id + " §7успешно добавлен!");
        
        return true;
    }

    @CmdInfo(name = "join", description = "Присоединиться к указанной арене.", usage = "/spleef join <арена>", permission = "")
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
            pl.sendMessage(BlockHuntManager.prefix + "§bОшибка: §7" + e.getMessage());
        }
        catch (ConsoleGameException e) {
            e.printStackTrace();
            pl.sendMessage(BlockHuntManager.prefix + "§cПроизошла ошибка, сообщите администрации!");
        }
        return true;
    }
        
    @CmdInfo(name = "leave", description = "Покинуть игру.", usage = "/spleef leave", permission = "")
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
            pl.sendMessage(BlockHuntManager.prefix + "§bОшибка: §7" + e.getMessage());
        }
        catch (ConsoleGameException e) {
            e.printStackTrace();
            pl.sendMessage(BlockHuntManager.prefix + "§cПроизошла ошибка, сообщите администрации!");
        }
        return true;
    }
    
    @CmdInfo(name = "list", description = "Показать список всех доступных арен.", usage = "/spleef list", permission = "")
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
            cs.sendMessage(BlockHuntManager.prefix + "Арена: '§b" + arena.getName() + "§7'. Статус: " + arena.getState().getName() + "§7. Игроков: " + BlockHuntManager.getRemainPlayersArena(arena));
        }
        cs.sendMessage(sep);
        
        return true;
    }
}

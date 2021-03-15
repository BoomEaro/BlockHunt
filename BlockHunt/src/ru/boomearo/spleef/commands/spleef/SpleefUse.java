package ru.boomearo.spleef.commands.spleef;

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

import ru.boomearo.gamecontrol.GameControl;
import ru.boomearo.gamecontrol.exceptions.ConsoleGameException;
import ru.boomearo.gamecontrol.exceptions.PlayerGameException;
import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.commands.CmdInfo;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefArena;
import ru.boomearo.spleef.objects.SpleefTeam;
import ru.boomearo.spleef.objects.region.CuboidRegion;

public class SpleefUse {


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
            pl.sendMessage(SpleefManager.prefix + "Выделите регион!");
            return true;
        }

        ConcurrentMap<Integer, SpleefTeam> teams = new ConcurrentHashMap<Integer, SpleefTeam>();
        
        int maxPlayers = 15;
        
        for (int i = 1; i <= maxPlayers; i++) {
            teams.put(i, new SpleefTeam(i, null));
        }
        
        try {
            SpleefArena newArena = new SpleefArena(arena, 2, maxPlayers, 300, pl.getWorld(), new CuboidRegion(re.getMaximumPoint(), re.getMinimumPoint(), pl.getWorld()), teams, pl.getLocation(), null);
            
            SpleefManager am = Spleef.getInstance().getSpleefManager();
            am.addArena(newArena);

            am.saveArenas();

            pl.sendMessage(SpleefManager.prefix + "Арена '§b" + arena + "§7' успешно создана!");
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

        SpleefManager trm = Spleef.getInstance().getSpleefManager();
        SpleefArena ar = trm.getGameArena(arena);
        if (ar == null) {
            cs.sendMessage(SpleefManager.prefix + "Арена '§b" + arena + "§7' не найдена!");
            return true;
        }
        
        Integer id = null;
        try {
            id = Integer.parseInt(args[1]);
        }
        catch (Exception e) {}
        if (id == null) {
            cs.sendMessage(SpleefManager.prefix + "Аргумент должен быть цифрой!");
            return true;
        }
        
        SpleefTeam team = ar.getTeamById(id);
        if (team == null) {
            cs.sendMessage(SpleefManager.prefix + "Команда §b" + id + " §7не найдена!");
            return true;
        }
        
        team.setSpawnPoint(pl.getLocation().clone());
        
        trm.saveArenas();
        
        cs.sendMessage(SpleefManager.prefix + "Спавн поинт §b" + id + " §7успешно добавлен!");
        
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
            GameControl.getInstance().getGameManager().joinGame(pl, Spleef.class, arena);
        } 
        catch (PlayerGameException e) {
            pl.sendMessage(SpleefManager.prefix + "§bОшибка: §7" + e.getMessage());
        }
        catch (ConsoleGameException e) {
            e.printStackTrace();
            pl.sendMessage(SpleefManager.prefix + "§cПроизошла ошибка, сообщите администрации!");
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
            pl.sendMessage(SpleefManager.prefix + "§bОшибка: §7" + e.getMessage());
        }
        catch (ConsoleGameException e) {
            e.printStackTrace();
            pl.sendMessage(SpleefManager.prefix + "§cПроизошла ошибка, сообщите администрации!");
        }
        return true;
    }
    
    @CmdInfo(name = "list", description = "Показать список всех доступных арен.", usage = "/spleef list", permission = "")
    public boolean list(CommandSender cs, String[] args) {
        if (args.length < 0 || args.length > 0) {
            return false;
        }
        
        Collection<SpleefArena> arenas = Spleef.getInstance().getSpleefManager().getAllArenas();
        if (arenas.isEmpty()) {
            cs.sendMessage(SpleefManager.prefix + "Арены еще не созданы!");
            return true;
        }
        final String sep = SpleefManager.prefix + "§8============================";
        cs.sendMessage(sep);
        for (SpleefArena arena : arenas) {
            cs.sendMessage(SpleefManager.prefix + "Арена: '§b" + arena.getName() + "§7'. Статус: " + arena.getState().getName() + "§7. Игроков: " + SpleefManager.getRemainPlayersArena(arena));
        }
        cs.sendMessage(sep);
        
        return true;
    }
}

package ru.boomearo.blockhunt.commands.blockhunt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;
import ru.boomearo.serverutils.utils.other.commands.AbstractExecutor;

public class CmdExecutorBlockHunt extends AbstractExecutor implements TabCompleter {

    private static final List<String> empty = new ArrayList<>();

    public CmdExecutorBlockHunt() {
        super(new BlockHuntUse());
    }

    @Override
    public boolean zeroArgument(CommandSender sender) {
        sendUsageCommands(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> ss = new ArrayList<>(Arrays.asList("join", "leave", "list"));
            if (sender.hasPermission("blockhunt.admin")) {
                ss.add("createarena");
                ss.add("setpoint");
            }
            List<String> matches = new ArrayList<>();
            String search = args[0].toLowerCase();
            for (String se : ss) {
                if (se.toLowerCase().startsWith(search)) {
                    matches.add(se);
                }
            }
            return matches;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("join")) {
                List<String> matches = new ArrayList<>();
                String search = args[1].toLowerCase();
                for (BHArena arena : BlockHunt.getInstance().getBlockHuntManager().getAllArenas()) {
                    if (arena.getName().toLowerCase().startsWith(search)) {
                        matches.add(arena.getName());
                    }
                }
                return matches;
            }
        }
        return empty;
    }

    @Override
    public String getPrefix() {
        return BlockHuntManager.prefix;
    }

    @Override
    public String getSuffix() {
        return " §8-" + BlockHuntManager.variableColor + " ";
    }
}

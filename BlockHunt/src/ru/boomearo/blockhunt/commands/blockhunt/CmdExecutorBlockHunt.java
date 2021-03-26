package ru.boomearo.blockhunt.commands.blockhunt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ru.boomearo.blockhunt.BlockHunt;
import ru.boomearo.blockhunt.commands.AbstractExecutor;
import ru.boomearo.blockhunt.commands.CmdList;
import ru.boomearo.blockhunt.managers.BlockHuntManager;
import ru.boomearo.blockhunt.objects.BHArena;

public class CmdExecutorBlockHunt extends AbstractExecutor {

	public CmdExecutorBlockHunt() {
		super(new BlockHuntUse());
	}

	@Override
	public boolean zeroArgument(CommandSender sender, CmdList cmds) {
		cmds.sendUsageCmds(sender);
		return true;
	}

	private static final List<String> empty = new ArrayList<>();

	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        if (arg3.length == 1) {
            List<String> ss = new ArrayList<String>(Arrays.asList("join", "leave", "list"));
            if (arg0.hasPermission("blockhunt.admin")) {
                ss.add("createarena");
                ss.add("setpoint");
            }
            List<String> matches = new ArrayList<>();
            String search = arg3[0].toLowerCase();
            for (String se : ss)
            {
                if (se.toLowerCase().startsWith(search))
                {
                    matches.add(se);
                }
            }
            return matches;
        }
        if (arg3.length == 2) {
            if (arg3[0].equalsIgnoreCase("join")) {
                List<String> ss = new ArrayList<String>();
                for (BHArena arena : BlockHunt.getInstance().getBlockHuntManager().getAllArenas()) {
                    ss.add(arena.getName());
                }
                List<String> matches = new ArrayList<>();
                String search = arg3[1].toLowerCase();
                for (String se : ss)
                {
                    if (se.toLowerCase().startsWith(search))
                    {
                        matches.add(se);
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
		return " §8-§7 ";
	}
}

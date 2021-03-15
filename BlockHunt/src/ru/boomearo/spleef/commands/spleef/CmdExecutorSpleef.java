package ru.boomearo.spleef.commands.spleef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import ru.boomearo.spleef.Spleef;
import ru.boomearo.spleef.commands.AbstractExecutor;
import ru.boomearo.spleef.commands.CmdList;
import ru.boomearo.spleef.managers.SpleefManager;
import ru.boomearo.spleef.objects.SpleefArena;

public class CmdExecutorSpleef extends AbstractExecutor {

	public CmdExecutorSpleef() {
		super(new SpleefUse());
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
            if (arg0.hasPermission("spleef.admin")) {
                ss.add("createarena");
                ss.add("setspawnpoint");
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
                for (SpleefArena arena : Spleef.getInstance().getSpleefManager().getAllArenas()) {
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
		return SpleefManager.prefix;
	}

	@Override
	public String getSuffix() {
		return " §8-§b ";
	}
}

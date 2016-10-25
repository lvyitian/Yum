package pw.yumc.Yum.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pw.yumc.Yum.api.YumAPI;
import pw.yumc.YumCore.commands.CommandArgument;
import pw.yumc.YumCore.commands.annotation.Tab;
import pw.yumc.YumCore.commands.interfaces.CommandExecutor;
import pw.yumc.YumCore.kit.StrKit;

/**
 *
 * @since 2016年7月7日 上午8:36:47
 * @author 喵♂呜
 */
public class PluginTabComplete implements CommandExecutor {
    public static PluginTabComplete instence = new PluginTabComplete();

    @Tab
    public List<String> listtab(CommandArgument e) {
        String[] args = e.getArgs();
        if (args[0].equalsIgnoreCase("install") || args[0].equalsIgnoreCase("i")) {
            return StrKit.copyPartialMatches(args[1], YumAPI.getRepo().getAllPluginName(), new ArrayList<String>());
        } else if (args[0].equalsIgnoreCase("repo") || args[0].equalsIgnoreCase("r")) {
            if (args.length == 2) { return StrKit.copyPartialMatches(args[1],
                    Arrays.asList(new String[] { "add", "all", "list", "delall", "clean", "update", "del" }),
                    new ArrayList<String>()); }
            if (args.length == 3 && (args[1] == "add" || args[1] == "del")) { return StrKit.copyPartialMatches(args[2],
                    YumAPI.getRepo().getRepos().keySet(),
                    new ArrayList<String>()); }
        } else if (args[0].equalsIgnoreCase("bukkitrepo") || args[0].equalsIgnoreCase("br")) {
            return StrKit.copyPartialMatches(args[1],
                    Arrays.asList(new String[] { "look", "install" }),
                    new ArrayList<String>());
        } else {
            return StrKit.copyPartialMatches(args[1],
                    YumAPI.getPlugman().getPluginNames(false),
                    new ArrayList<String>());
        }
        return null;
    }
}

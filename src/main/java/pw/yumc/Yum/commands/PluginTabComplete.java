package pw.yumc.Yum.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.citycraft.PluginHelper.commands.HandlerCommands;
import cn.citycraft.PluginHelper.commands.HandlerTabComplete;
import cn.citycraft.PluginHelper.commands.InvokeCommandEvent;
import cn.citycraft.PluginHelper.utils.StrKit;
import pw.yumc.Yum.api.YumAPI;

/**
 *
 * @since 2016年7月7日 上午8:36:47
 * @author 喵♂呜
 */
public class PluginTabComplete implements HandlerCommands {
    public static PluginTabComplete instence = new PluginTabComplete();

    @HandlerTabComplete()
    public List<String> listtab(final InvokeCommandEvent e) {
        final String[] args = e.getArgs();
        if (args[0].equalsIgnoreCase("install") || args[0].equalsIgnoreCase("i")) {
            return StrKit.copyPartialMatches(args[1], YumAPI.getRepo().getAllPluginName(), new ArrayList<String>());
        } else if (args[0].equalsIgnoreCase("repo")) {
            if (args.length == 2) {
                return StrKit.copyPartialMatches(args[1], Arrays.asList(new String[] { "add", "all", "list", "delall", "clean", "update", "del" }), new ArrayList<String>());
            }
            if (args.length == 3 && (args[1] == "add" || args[1] == "del")) {
                return StrKit.copyPartialMatches(args[2], YumAPI.getRepo().getRepos().keySet(), new ArrayList<String>());
            }
        } else {
            return StrKit.copyPartialMatches(args[1], YumAPI.getPlugman().getPluginNames(false), new ArrayList<String>());
        }
        return null;
    }
}

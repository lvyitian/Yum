/**
 *
 */
package cn.citycraft.Yum.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.Yum.Yum;
import cn.citycraft.Yum.api.YumAPI;

/**
 * 插件删除命令类
 *
 * @author 喵♂呜
 * @since 2015年8月12日下午2:04:05
 */
public class CommandDelete extends BaseCommand {
    Yum main;

    public CommandDelete(final Yum main) {
        super("del");
        this.main = main;
        setMinimumArguments(1);
        setDescription("删除插件");
        setPossibleArguments("<插件名称>");
    }

    @Override
    public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
        final String pluginname = args[0];
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginname);
        if (plugin != null) {
            final String version = plugin.getDescription().getVersion();
            if (YumAPI.getPlugman().deletePlugin(sender, plugin)) {
                sender.sendMessage("§c删除: §a插件 §6" + pluginname + " §a版本 §d" + version + " §a已从服务器卸载并删除!");
            } else {
                sender.sendMessage("§c删除: §c插件 " + pluginname + " 卸载或删除时发生错误 删除失败!");
            }
        } else {
            sender.sendMessage("§c插件 " + pluginname + " 不存在或已卸载!");
        }
    }
}

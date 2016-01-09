/**
 *
 */
package cn.citycraft.Yum.file.commands;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.PluginHelper.utils.FileUtil;
import cn.citycraft.Yum.Yum;

/**
 * 插件删除命令类
 *
 * @author 喵♂呜
 * @since 2015年8月12日下午2:04:05
 */
public class CommandRm extends BaseCommand {
    Yum plugin;

    public CommandRm(final Yum main) {
        super();
        plugin = main;
        setMinimumArguments(1);
        setDescription("删除文件夹(服务器JAR为根目录)");
        setPossibleArguments("<文件相对目录>");
    }

    @Override
    public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
        final String fpath = args[0];
        final File file = new File(fpath);
        if (!file.exists()) {
            sendMessage(sender, "§c目录 " + file.getAbsolutePath() + " 不存在!");
        } else {
            if (!file.isDirectory()) {
                sendMessage(sender, "§d路径 §e" + file.getAbsolutePath() + " §c是一个文件 请使用file delete!");
                return;
            }
            for (final String name : plugin.config.getStringList("blacklist")) {
                if (file.getAbsolutePath().toLowerCase().endsWith(name)) {
                    sendMessage(sender, "§d路径 §e" + file.getAbsolutePath() + " §c不允许被删除!");
                    return;
                }
            }
            if (file.listFiles().length != 0 && !(args.length > 1 && args[1].equalsIgnoreCase("-rf"))) {
                sendMessage(sender, "§d目录 §e" + file.getAbsolutePath() + " §c不为空!");
                sendMessage(sender, "§c请使用 §a/file rm " + fpath + " -rf §c强行删除!");
                return;
            }
            sendMessage(sender, "§d目录 §e" + file.getAbsolutePath() + " " + (FileUtil.deleteDir(sender, file) ? "§a删除成功!" : "§c删除失败!"));
        }
    }
}

/**
 *
 */
package cn.citycraft.Yum.file.commands;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import cn.citycraft.PluginHelper.commands.BaseCommand;
import cn.citycraft.Yum.Yum;

/**
 * 插件删除命令类
 *
 * @author 喵♂呜
 * @since 2015年8月12日下午2:04:05
 */
public class CommandLs extends BaseCommand {

    public CommandLs(final Yum main) {
        super("l");
        setDescription("列出当前目录(服务器JAR为根目录)");
        setPossibleArguments("<相对目录>");
    }

    @Override
    public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
        File dir = new File(".");
        if (args.length == 1) {
            dir = new File(args[0]);
        }
        if (!dir.isDirectory()) {
            sendMessage(sender, "§6路径: §e " + dir.getAbsolutePath() + " §c不是一个目录!");
            return;
        }
        final StringBuffer sb = new StringBuffer();
        for (final File file : dir.listFiles()) {
            if (file.isDirectory()) {
                sb.append("§b");
            } else {
                sb.append("§a");
            }
            sb.append(file.getName() + " ");
        }
        final String filelist = sb.toString();
        if (filelist.isEmpty()) {
            sendMessage(sender, "§6目录: §e" + dir.getAbsolutePath() + " §c下没有文件或文件夹!");
        } else {
            sendMessage(sender, "§6目录: §e" + dir.getAbsolutePath() + " §a存在如下文件!");
            sendMessage(sender, sb.toString());
        }
    }
}

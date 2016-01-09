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
public class CommandDelete extends BaseCommand {
    public CommandDelete(final Yum main) {
        super("del");
        setMinimumArguments(1);
        setDescription("删除文件(服务器JAR为根目录)");
        setPossibleArguments("<文件相对目录>");
    }

    @Override
    public void execute(final CommandSender sender, final Command command, final String label, final String[] args) throws CommandException {
        final String fpath = args[0];
        final File file = new File(fpath);
        if (!file.exists()) {
            sendMessage(sender, "§c文件 " + file.getAbsolutePath() + " 不存在!");
        } else {
            if (file.isDirectory()) {
                sendMessage(sender, "§e" + file.getAbsolutePath() + " §c是一个目录 请使用file rm!");
                return;
            }
            try {
                sendMessage(sender, "§d文件 §e" + file.getAbsolutePath() + " " + (file.delete() ? "§a删除成功!" : "§c删除失败!"));
            } catch (final Exception e) {
                sendMessage(sender, "§d文件 §e" + file.getAbsolutePath() + " 删除失败: " + e.getMessage());
            }
        }
    }
}

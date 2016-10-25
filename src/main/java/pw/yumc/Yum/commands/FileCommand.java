package pw.yumc.Yum.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import pw.yumc.Yum.Yum;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.YumCore.commands.CommandManager;
import pw.yumc.YumCore.commands.annotation.Async;
import pw.yumc.YumCore.commands.annotation.Cmd;
import pw.yumc.YumCore.commands.annotation.Help;
import pw.yumc.YumCore.commands.annotation.KeyValue;
import pw.yumc.YumCore.commands.interfaces.CommandExecutor;
import pw.yumc.YumCore.kit.FileKit;

/**
 * File命令基类
 *
 * @since 2016年1月9日 上午10:02:39
 * @author 喵♂呜
 */
public class FileCommand implements CommandExecutor {
    private static String prefix = "§6[§bYum §a文件管理§6] ";

    private static String file_not_found = prefix + "§b%s §c文件未找到!";
    private static String file_is_dir = prefix + "§b%s §c文件是一个目录!";

    private static String copySuccess = prefix + "§a文件 §b%s §a复制成功!";
    private static String copyFailed = prefix + "§c文件 §b%s §c复制失败!";

    private static String waitCommand = prefix + "§a命令已发送,请等待执行完毕...";
    private static String runResult = prefix + "§a命令运行结果如下:";
    private static String noResult = prefix + "§d当前命令没有返回结果!";
    private static String runError = prefix + "§c命令运行错误: %s %s";

    private static String addStartupSuccess = "§e %s §a成功添加开机自动启...";
    private static String addStartupFailed = "§c添加开机自动启失败 %s %s!";
    private static String STARTPATH = "C:\\ProgramData\\Microsoft\\Windows\\Start Menu\\Programs\\StartUp";

    Yum plugin;

    public FileCommand(Yum yum) {
        plugin = yum;
        new CommandManager("file", this, PluginTabComplete.instence);
    }

    @Cmd(aliases = "cp", minimumArguments = 2)
    @Help(value = "复制文件", possibleArguments = "<源文件> <目标目录>")
    @Async
    public void copy(CommandSender sender, @KeyValue(key = "check") File src, File des) throws FileNotFoundException, IOException {
        if (src.isDirectory()) {
            sender.sendMessage(String.format(file_is_dir, src.getPath()));
            return;
        }
        if (Files.copy(new FileInputStream(src), des.toPath(), StandardCopyOption.REPLACE_EXISTING) != 0) {
            sender.sendMessage(String.format(copySuccess, src.getPath()));
        } else {
            sender.sendMessage(String.format(copyFailed, src.getPath()));
        }
    }

    @Cmd(aliases = "del", minimumArguments = 1)
    @Help(value = "删除文件(服务器JAR为根目录)", possibleArguments = "<文件相对目录>")
    @Async
    public void delete(CommandSender sender, String fpath) {
        File file = new File(fpath);
        fpath = file.getAbsolutePath();
        if (!file.exists()) {
            sender.sendMessage(String.format(file_not_found, fpath));
        } else {
            if (file.isDirectory()) {
                sender.sendMessage(String.format(file_is_dir, fpath));
                return;
            }
            try {
                sender.sendMessage("§d文件 §e" + fpath + " " + (file.delete() ? "§a删除成功!" : "§c删除失败!"));
            } catch (Exception ex) {
                sender.sendMessage("§d文件 §e" + fpath + " 删除失败: " + ex.getMessage());
            }
        }
    }

    @Cmd(aliases = "d", minimumArguments = 1)
    @Help(value = "下载文件(默认保存到服务器更新文件夹)", possibleArguments = "<下载地址> [保存文件路径]")
    @Async
    public void download(CommandSender sender, String urlstr, String path) {
        if (!urlstr.startsWith("http")) {
            urlstr = "http://" + urlstr;
        }
        File file = null;
        if (path != null) {
            file = new File(path);
        } else {
            file = new File(Bukkit.getUpdateFolderFile(), YumAPI.getDownload().getFileName(urlstr));
        }
        YumAPI.getDownload().run(sender, urlstr, file);
    }

    @Cmd(aliases = "ls")
    @Help(value = "列出当前目录(服务器JAR为根目录)", possibleArguments = "<相对目录>")
    @Async
    public void ls(CommandSender sender, String filename) {
        File dir = new File(".");
        if (filename != null) {
            dir = new File(filename);
        }
        if (!dir.isDirectory()) {
            sender.sendMessage("§6路径: §e " + dir.getAbsolutePath() + " §c不是一个目录!");
            return;
        }
        StringBuffer sb = new StringBuffer();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                sb.append("§b");
            } else {
                sb.append("§a");
            }
            sb.append(file.getName() + " ");
        }
        String filelist = sb.toString();
        if (filelist.isEmpty()) {
            sender.sendMessage("§6目录: §e" + dir.getAbsolutePath() + " §c下没有文件或文件夹!");
        } else {
            sender.sendMessage("§6目录: §e" + dir.getAbsolutePath() + " §a存在如下文件!");
            sender.sendMessage(sb.toString());
        }
    }

    @Cmd(aliases = "rn", minimumArguments = 2)
    @Help(value = "重命名文件(服务器JAR为根目录)", possibleArguments = "<文件相对路径> <文件名称>")
    @Async
    public void rename(CommandSender sender, String fpath, String des) {
        File file = new File(fpath);
        if (!file.exists()) {
            sender.sendMessage("§c文件 " + file.getAbsolutePath() + " 不存在!");
        } else {
            try {
                File newFile = new File(file.getParentFile(), des);
                file.renameTo(newFile);
                sender.sendMessage("§a文件 §e" + file.getAbsolutePath() + " §a重命名为 §d" + newFile.getAbsolutePath());
            } catch (Exception ex) {
                sender.sendMessage("§c文件 §e" + file.getAbsolutePath() + " §c重命名失败: " + ex.getMessage());
            }
        }
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "删除文件夹(服务器JAR为根目录)", possibleArguments = "<相对目录>")
    @Async
    public void rm(CommandSender sender, String fpath, String option) {
        File file = new File(fpath);
        if (!file.exists()) {
            sender.sendMessage("§c目录 " + file.getAbsolutePath() + " 不存在!");
        } else {
            if (!file.isDirectory()) {
                sender.sendMessage("§d路径 §e" + file.getAbsolutePath() + " §c是一个文件 请使用file delete!");
                return;
            }
            for (String name : plugin.getConfig().getStringList("blacklist")) {
                if (file.getAbsolutePath().toLowerCase().endsWith(name)) {
                    sender.sendMessage("§d路径 §e" + file.getAbsolutePath() + " §c不允许被删除!");
                    return;
                }
            }
            if (file.listFiles().length != 0 && !(option != null && option.equalsIgnoreCase("-rf"))) {
                sender.sendMessage("§d目录 §e" + file.getAbsolutePath() + " §c不为空!");
                sender.sendMessage("§c请使用 §a/file rm " + fpath + " -rf §c强行删除!");
                return;
            }
            sender.sendMessage("§d目录 §e" + file.getAbsolutePath() + " "
                    + (FileKit.deleteDir(sender, file) ? "§a删除成功!" : "§c删除失败!"));
        }
    }

    @Cmd(aliases = "r", minimumArguments = 1)
    @Help(value = "运行一个命令或文件", possibleArguments = "<命令或文件绝对路径>")
    @Async
    public void run(CommandSender sender, String args) {
        try {
            Process process = Runtime.getRuntime().exec(args);
            sender.sendMessage(waitCommand);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            StringBuilder build = new StringBuilder();
            while ((line = br.readLine()) != null) {
                build.append(line);
                build.append("\n");
            }
            if (build.toString().replace("\n", "").isEmpty()) {
                sender.sendMessage(noResult);
            } else {
                sender.sendMessage(runResult);
                sender.sendMessage(build.toString().split("\n"));
            }
        } catch (Exception e2) {
            sender.sendMessage(String.format(runError, e2.getClass().getSimpleName(), e2.getMessage()));
        }
    }

    @Cmd(minimumArguments = 1)
    @Help(value = "添加开机自启动", possibleArguments = "<文件绝对路径>")
    @Async
    public void startup(CommandSender sender, String filepath) {
        File src = new File(filepath);
        File des = new File(STARTPATH, src.getName());
        if (!src.exists()) {
            sender.sendMessage(String.format(file_not_found, filepath));
            return;
        }
        if (src.isDirectory()) {
            sender.sendMessage(String.format(file_is_dir, filepath));
            return;
        }
        try {
            Files.copy(new FileInputStream(src), des.toPath(), StandardCopyOption.REPLACE_EXISTING);
            sender.sendMessage(String.format(addStartupSuccess, filepath));
        } catch (Exception e2) {
            sender.sendMessage(String.format(addStartupFailed, e2.getClass().getName(), e2.getMessage()));
        }
    }
}

/**
 *
 */
package pw.yumc.Yum;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import cn.citycraft.CommonData.UpdatePlugin;
import cn.citycraft.PluginHelper.config.FileConfig;
import cn.citycraft.PluginHelper.utils.VersionChecker;
import pw.yumc.Yum.api.YumAPI;
import pw.yumc.Yum.commands.FileCommand;
import pw.yumc.Yum.commands.NetCommand;
import pw.yumc.Yum.commands.YumCommand;
import pw.yumc.Yum.listeners.PluginNetworkListener;
import pw.yumc.Yum.listeners.SecurityListener;
import pw.yumc.Yum.managers.ConfigManager;
import pw.yumc.Yum.managers.NetworkManager;

/**
 * MC插件仓库
 *
 * @author 喵♂呜
 * @since 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
    FileConfig config;
    NetworkManager netmgr;

    @Override
    public FileConfig getConfig() {
        return config;
    }

    public void initCommands() {
        new YumCommand(this);
        new NetCommand(this);
        new FileCommand(this);
    }

    public void initListeners() {
        new SecurityListener(this);
        new PluginNetworkListener(this);
    }

    @Override
    public void onDisable() {
        netmgr.unregister();
    }

    @Override
    public void onEnable() {
        new YumAPI(this);
        initCommands();
        initListeners();
        new VersionChecker(this);
        YumAPI.updaterepo(Bukkit.getConsoleSender());
        YumAPI.updatecheck(Bukkit.getConsoleSender());
    }

    @Override
    public void onLoad() {
        config = new FileConfig(this);
        // 初始化配置
        ConfigManager.init(getConfig());
        // 初始化更新列
        UpdatePlugin.getUpdateList();
        // 启用网络注入
        netmgr = new NetworkManager().register(this);
    }
}

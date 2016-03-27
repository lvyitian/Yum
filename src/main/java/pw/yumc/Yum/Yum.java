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
import pw.yumc.Yum.commands.YumCommand;
import pw.yumc.Yum.manager.NetworkManager;

/**
 * MC插件仓库
 *
 * @author 喵♂呜
 * @since 2015年8月21日下午5:14:39
 */
public class Yum extends JavaPlugin {
    public FileConfig config;
    NetworkManager netmgr;

    @Override
    public void onDisable() {
        netmgr.unregister();
    }

    @Override
    public void onEnable() {
        new YumAPI(this);
        new YumCommand(this);
        new FileCommand(this);
        new VersionChecker(this);
        netmgr = new NetworkManager(this);
        netmgr.setDebug(true).register();
        YumAPI.updaterepo(Bukkit.getConsoleSender());
        YumAPI.updatecheck(Bukkit.getConsoleSender());
    }

    @Override
    public void onLoad() {
        config = new FileConfig(this);
        // 初始化更新列
        UpdatePlugin.getUpdateList();
    }
}

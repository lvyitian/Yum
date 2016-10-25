package pw.yumc.Yum.managers;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import org.bukkit.Bukkit;

import pw.yumc.Yum.Yum;
import pw.yumc.Yum.events.PluginNetworkEvent;
import pw.yumc.YumCore.kit.ExKit;
import pw.yumc.YumCore.kit.PKit;

/**
 * 网络代理处理类
 *
 * @since 2016年3月26日 下午4:24:09
 * @author 喵♂呜
 */
public class NetworkManager {

    public static void register(Yum plugin) {
        Bukkit.getConsoleSender().sendMessage("§6[§bYum §a网络管理§6] §a注入网络管理系统 将托管服务器网络!");
        ProxySelector.setDefault(new YumProxySelector(ProxySelector.getDefault(), plugin));
    }

    public static void unregister() {
        ProxySelector cur = ProxySelector.getDefault();
        if (cur instanceof YumProxySelector) {
            ProxySelector.setDefault(((YumProxySelector) cur).getDefaultSelector());
        }
    }

    static class YumProxySelector extends ProxySelector {
        private ProxySelector defaultSelector;

        public YumProxySelector(ProxySelector defaultSelector, Yum plugin) {
            this.defaultSelector = defaultSelector;
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            defaultSelector.connectFailed(uri, sa, ioe);
        }

        public ProxySelector getDefaultSelector() {
            return defaultSelector;
        }

        @Override
        public List<Proxy> select(URI uri) {
            PluginNetworkEvent pne = new PluginNetworkEvent(PKit.getOperatePlugin(), uri, Bukkit.isPrimaryThread());
            Bukkit.getPluginManager().callEvent(pne);
            if (pne.isCancelled()) {
                ExKit.throwException(new IOException("[Yum 网络防护] 已开启网络防护 并被联网规则拦截!"));
            }
            return defaultSelector.select(uri);
        }

    }

}

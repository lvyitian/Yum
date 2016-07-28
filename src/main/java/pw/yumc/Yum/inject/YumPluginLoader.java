/**
 * Created on 17 May 2014 by _MylesC
 * Copyright 2014
 */
package pw.yumc.Yum.inject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

public class YumPluginLoader implements PluginLoader {
    private static boolean isInit = false;
    private static final String needRestart = "§6[§bYum§6] §c由于修改了服务器内部文件 §bYum §c无法直接重载 §4请重启服务器!";
    private static final YumPluginLoader yumPluginLoader = new YumPluginLoader(Bukkit.getServer());
    private final JavaPluginLoader internal_loader;
    private final Server server;

    @SuppressWarnings("deprecation")
    public YumPluginLoader(final Server instance) {
        server = instance;
        internal_loader = new JavaPluginLoader(instance);
    }

    public static void inject() {
        injectExistingPlugins(yumPluginLoader);
        replaceJavaPluginLoaders(yumPluginLoader);
    }

    private static void injectExistingPlugins(final YumPluginLoader yumPluginLoader) {
        for (final org.bukkit.plugin.Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (p instanceof JavaPlugin) {
                final JavaPlugin jp = (JavaPlugin) p;
                try {
                    final Field f = JavaPlugin.class.getDeclaredField("loader");
                    f.setAccessible(true);
                    f.set(jp, yumPluginLoader);
                } catch (final Exception e) {
                    Bukkit.getServer().getLogger().log(Level.SEVERE, "Yum failed injecting " + jp.getDescription().getFullName() + " with the new PluginLoader, contact the developers on YUMC!", e);
                }
            }
        }
    }

    private static void replaceJavaPluginLoaders(final YumPluginLoader yumPluginLoader) {
        final PluginManager spm = Bukkit.getPluginManager();
        try {
            final Field field = spm.getClass().getDeclaredField("fileAssociations");
            field.setAccessible(true);
            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            @SuppressWarnings("unchecked")
            final Map<Pattern, PluginLoader> map = (Map<Pattern, PluginLoader>) field.get(spm);
            final Iterator<Map.Entry<Pattern, PluginLoader>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                final Entry<Pattern, PluginLoader> entry = iter.next();
                if (entry.getValue() instanceof JavaPluginLoader) {
                    entry.setValue(yumPluginLoader);
                }
            }
            field.set(spm, map);
        } catch (final Exception e) {
            Bukkit.getServer().getLogger().log(Level.SEVERE, "Yum failed replacing the existing PluginLoader, contact the developers on YUMC!", e);
        }
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(final Listener listener, final Plugin plugin) {
        return internal_loader.createRegisteredListeners(listener, plugin);
    }

    @Override
    public void disablePlugin(final Plugin plugin) {
        if (plugin.getName().equalsIgnoreCase("Yum")) {
            Bukkit.getConsoleSender().sendMessage(needRestart);
        } else {
            internal_loader.disablePlugin(plugin);
        }
    }

    @Override
    public void enablePlugin(final Plugin plugin) {
        if (plugin.getName().equalsIgnoreCase("Yum")) {
            if (isInit) {
                Bukkit.getConsoleSender().sendMessage(needRestart);
            } else {
                internal_loader.enablePlugin(plugin);
                isInit = true;
            }
        } else {
            internal_loader.enablePlugin(plugin);
        }
    }

    @Override
    public PluginDescriptionFile getPluginDescription(final File arg0) throws InvalidDescriptionException {
        return internal_loader.getPluginDescription(arg0);
    }

    @Override
    public Pattern[] getPluginFileFilters() {
        return internal_loader.getPluginFileFilters();
    }

    @Override
    public Plugin loadPlugin(final File file) throws InvalidPluginException, UnknownDependencyException {
        try {
            final PluginDescriptionFile description = getPluginDescription(file);
            if (description.getName().equalsIgnoreCase("Yum")) {
                Bukkit.getConsoleSender().sendMessage(needRestart);
                return null;
            }
        } catch (final InvalidDescriptionException ex) {
        }
        return internal_loader.loadPlugin(file);
    }

}

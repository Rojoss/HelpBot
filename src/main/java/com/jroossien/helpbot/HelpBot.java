/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Rojoss <http://jroossien.com>
 * Copyright (c) 2016 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.jroossien.helpbot;

import com.jroossien.helpbot.config.PluginCfg;
import com.jroossien.helpbot.listeners.MainListener;
import com.jroossien.helpbot.messages.Language;
import com.jroossien.helpbot.messages.MessageConfig;
import com.jroossien.helpbot.nms.NMS;
import com.jroossien.helpbot.nms.NMSVersion;
import com.jroossien.helpbot.utils.Parse;
import com.jroossien.helpbot.utils.item.GlowEnchant;
import com.jroossien.helpbot.utils.menu.Menu;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class HelpBot extends JavaPlugin {

    private static HelpBot instance;

    private Language language = null;

    private PluginCfg cfg;

    private final Logger log = Logger.getLogger("HelpBot");

    @Override
    public void onDisable() {
        GlowEnchant.unregister();
        instance = null;
        log("disabled");
    }

    @Override
    public void onEnable() {
        instance = this;
        log.setParent(this.getLogger());

        if (!NMS.get().isCompatible()) {
            error("Failed to load HelpBot because your server version isn't supported!");
            error("This version of HelpBot supports the following server versions: " + Parse.Array((Object[]) NMSVersion.values()));
            error("Your server version: " + NMS.get().getVersionString());
            getPluginLoader().disablePlugin(this);
            return;
        }

        cfg = new PluginCfg("plugins/HelpBot/plugin.yml");

        if (!setupLanguage()) {
            warn("Invalid language specified in the config. Falling back to " + language.getName() + " [" + language.getID() + "]!");
        } else {
            log("Using " + language.getName() + " [" + language.getID() + "] as language!");
        }
        loadMessages();

        registerCommands();
        registerListeners();

        log("loaded successfully");
    }

    private void registerCommands() {

    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new Menu.Events(), this);
        getServer().getPluginManager().registerEvents(new MainListener(this), this);
    }

    public boolean setupLanguage() {
        language = Language.find(getCfg().language);
        if (language == null) {
            language = Language.ENGLISH;
            return false;
        }
        return true;
    }

    private void loadMessages() {
        new MessageConfig(this, "messages");
        new MessageConfig(this, "commands");
        new MessageConfig(this, "parser");
    }

    public void log(Object msg) {
        log.info("[HelpBot] " + msg.toString());
    }

    public void warn(Object msg) {
        log.warning("[HelpBot] " + msg.toString());
    }

    public void error(Object msg) {
        log.severe("[HelpBot] " + msg.toString());
    }

    public static HelpBot get() {
        return instance;
    }

    /**
     * Get the language used for messages.
     * If there was an error loading language files it will use the fall back english.
     *
     * @return active language used for messages.
     */
    public Language getLanguage() {
        if (language == null) {
            return Language.ENGLISH;
        }
        return language;
    }

    public PluginCfg getCfg() {
        return cfg;
    }
}

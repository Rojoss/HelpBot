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

package com.jroossien.helpbot.nms;

import com.jroossien.helpbot.HelpBot;
import com.jroossien.helpbot.nms.annotation.NMSDependant;
import com.jroossien.helpbot.nms.chat.Chat;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;

public class NMS {

    private static NMS instance = null;
    private String versionString = "unknown";
    private NMSVersion version;

    private Chat chat;

    private NMS() {
        try {
            versionString = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

            version = NMSVersion.fromString(versionString);

            chat = (Chat) loadFromNMS(Chat.class);

        } catch (ArrayIndexOutOfBoundsException ignored) {}
    }


    public Chat getChat() {
        return chat;
    }


    public NMSVersion getVersion() {
        return version;
    }

    public String getVersionString() {
        return versionString;
    }

    public boolean isCompatible() {
        return version != null;
    }

    public static NMS get() {
        if (instance == null) {
            instance = new NMS();
        }
        return instance;
    }

    public <T> Object loadFromNMS(Class<T> dep) {
        if (!dep.isAnnotationPresent(NMSDependant.class)) return null;
        NMSDependant nmsDependant = dep.getAnnotation(NMSDependant.class);
        Class<?> impl = null;
        try {
            impl = Class.forName(nmsDependant.implementationPath() + "." + dep.getSimpleName() + "_" + version);
            return impl.newInstance();
        } catch (ClassNotFoundException e) {
            HelpBot.get().error("The current version is not supported: " + version + ".\n" + e.getMessage());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return impl;
    }

    public <T> Object loadFromNMS(Class<T> dep, Object... objects) {
        if (!dep.isAnnotationPresent(NMSDependant.class)) return null;
        NMSDependant nmsDependant = dep.getAnnotation(NMSDependant.class);
        Class<?> impl = null;
        try {
            impl = Class.forName(nmsDependant.implementationPath() + "." + dep.getSimpleName() + "_" + version);
            return ConstructorUtils.invokeConstructor(impl, objects);
        } catch (ClassNotFoundException e) {
            HelpBot.get().error("The current version is not supported: " + version + ".\n" + e.getMessage());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return impl;
    }

}

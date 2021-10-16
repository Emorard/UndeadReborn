package com.pokkedoll.undeadreborn;

import clojure.java.api.Clojure;
import clojure.lang.Compiler;
import clojure.lang.DynamicClassLoader;
import clojure.lang.IFn;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class Main extends JavaPlugin {

  private static Main instance;

  public static Main getInstance() {
    return instance;
  }

  static {
    final ClassLoader cl = Main.class.getClassLoader();
    Thread.currentThread().setContextClassLoader(cl);
    DynamicClassLoader newcl = (DynamicClassLoader) AccessController.doPrivileged((PrivilegedAction<Object>) () -> new DynamicClassLoader(cl));
    clojure.lang.RT.init();
    clojure.lang.Var.pushThreadBindings(clojure.lang.RT.map(Compiler.LOADER, newcl));
  }

  @Override
  public void onEnable() {
    instance = this;
    String pluginName = getDescription().getName();
    loadClojureNameSpace(pluginName + ".core");
    invokeClojureFunction(pluginName + ".core", "on-enable");
  }

  @Override
  public void onDisable() {
    String pluginName = getDescription().getName();
    invokeClojureFunction(pluginName + ".core", "on-disable");
  }

  @Override
  public void onLoad() {
    URL jarURL;
    try {
      jarURL = this.getFile().toURI().toURL();
    } catch (MalformedURLException e) {
      return;
    }
    ((DynamicClassLoader) Compiler.LOADER.deref()).addURL(jarURL);
  }

  private void loadClojureNameSpace(String ns) {
    IFn require = Clojure.var("clojure.core", "require");
    Object cljFileRead = Clojure.read(ns);
    require.invoke(cljFileRead);
  }

  private Object invokeClojureFunction(String ns, String function) {
    return Clojure.var(ns, function).invoke(this);
  }

  public static String[] test() {
    return new String[] {"1", "2", "3"};
  }
}

package com.java_craft.mc.JCVaultListener;

import com.java_craft.mc.JCVaultListener.config.ConfigurationSet;
import com.java_craft.mc.JCVaultListener.listeners.VotifierListener;
import com.java_craft.mc.JCVaultListener.logging.LogFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class JCVaultListener extends JavaPlugin
{
  private static final Logger LOGGER = Logger.getLogger("JCVaultListener");
  private ConfigurationSet configuration;
  private Economy economy = null;

  public void onEnable()
  {
    try
    {
      this.configuration = new ConfigurationSet(this);
    }
    catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error reading configuration file!", e);
      LOGGER.severe("JCVaultListener IS NOT enabled");
      return;
    }

    initializeEconomyAPI();

    getServer().getPluginManager().registerEvents(new VotifierListener(this), this);

    LOGGER.info("JCVaultListener enabled!");
  }

  public void onDisable()
  {
    LOGGER.info("JCVaultListener disabled!");
  }

  private void initializeEconomyAPI()
  {
    try {
	  RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);

      if (economyProvider != null) {
        this.economy = ((Economy)economyProvider.getProvider());
        LOGGER.info("Using economy plugin: " + this.economy.getName());
      }
      else {
        this.economy = null;
        LOGGER.severe("Vault cannot detect a valid economy plugin. No payments will be made!");
      }
    }
    catch (NoClassDefFoundError ex) {
      LOGGER.severe("Could not find Vault API. Please make sure Vault is installed and enabled!");
    }
  }

  public ConfigurationSet getConfiguration()
  {
    return this.configuration;
  }

  public Economy getEconomy()
  {
    return this.economy;
  }

  static
  {
    LOGGER.setFilter(new LogFilter("[JCVaultListener] "));
  }
}
package com.java_craft.mc.JCVaultListener.config;

import com.java_craft.mc.JCVaultListener.JCVaultListener;
import com.java_craft.mc.JCVaultListener.model.IVaultListenerConstants;
import com.java_craft.mc.JCVaultListener.model.PayType;
import com.java_craft.mc.JCVaultListener.model.VoteService;
import com.java_craft.mc.JCVaultListener.utils.ConfigTemplate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigurationSet
  implements IVaultListenerConstants
{
  private static final Logger LOGGER = Logger.getLogger("JCVaultListener");
  private final JCVaultListener plugin;
  private boolean debug = false;

  private boolean broadcastFlag = true;

  private boolean checkPlayer = false;

  private String confirmMsg = "Thanks {IGN}, for voting on {SERVICE}!";

  private String paymentMsg = "{AMOUNT} has been added to your {ECONOMY} balance.";

  private String broadcastMsg = "The server was voted for by {IGN}!";

  private String prefix = "";

  private String suffix = "";

  private Map<String, VoteService> vsConfigs = new HashMap<String, VoteService>();

  public ConfigurationSet(JCVaultListener plugin)
    throws InvalidConfigurationException, IOException
  {
    this.plugin = plugin;
    this.vsConfigs.put("default", new VoteService("default"));
    loadConfiguration();
  }

  public VoteService getVoteService(String svcName)
  {
    svcName = svcName.toLowerCase();
    VoteService vs = (VoteService)this.vsConfigs.get(svcName);
    if (vs == null)
      vs = (VoteService)this.vsConfigs.get("default");
    return vs;
  }

  private void loadConfiguration()
    throws InvalidConfigurationException, IOException
  {
    File oldPropFile = new File("plugins/Votifier/VaultListener.properties");
    File oldVLClass = new File("plugins/Votifier/listeners/VaultListener.class");

    File cfgFile = new File(this.plugin.getDataFolder(), "config.yml");
    if (cfgFile.exists()) {
      readConfiguration(cfgFile);
    }
    else {
      if (oldPropFile.exists())
        importProperties(oldPropFile);
      saveConfiguration();
    }

    if ((oldPropFile.exists()) || (oldVLClass.exists()))
      LOGGER.warning("Please make sure to delete the old 'VaultListener.class' from Votifier's listener directory and/or 'VaultListener.properties' from the data directory!");
  }

  private void importProperties(File propFile)
  {
    Properties props = new Properties();

    LOGGER.info("Importing old VaultListener properties");
    try {
      FileReader freader = new FileReader(propFile);
      props.load(freader);
      freader.close();
    }
    catch (IOException ex) {
      LOGGER.warning("Unable to import old VaultListener properties. Using default configuration!");
      return;
    }

    this.debug = Boolean.parseBoolean(props.getProperty("debug", "false"));
    this.broadcastFlag = Boolean.parseBoolean(props.getProperty("broadcast", "true"));
    this.confirmMsg = props.getProperty("confirm_msg", this.confirmMsg);
    this.paymentMsg = props.getProperty("payment_msg", this.paymentMsg);
    this.broadcastMsg = props.getProperty("broadcast_msg", this.broadcastMsg);
    this.prefix = props.getProperty("reward_prefix", this.prefix);
    this.suffix = props.getProperty("reward_suffix", this.suffix);

    double amount = 30.0D;
    try {
      amount = Double.parseDouble(props.getProperty("reward_amount", Double.toString(30.0D)));
    }
    catch (NumberFormatException ex)
    {
      amount = 30.0D;
    }

    PayType type = PayType.FIXED;
    String strType = props.getProperty("reward_type", "FIXED").toUpperCase();
    try {
      type = PayType.valueOf(strType);
    }
    catch (IllegalArgumentException ex) {
      type = PayType.FIXED;
    }

    double rate = 0.01D;
    try {
      rate = Double.parseDouble(props.getProperty("reward_rate", Double.toString(0.01D)));
    }
    catch (NumberFormatException ex)
    {
      rate = 0.01D;
    }

    this.vsConfigs.put("default", new VoteService("default", amount, rate, type));
  }

  private void readConfiguration(File cfgFile)
    throws InvalidConfigurationException, IOException
  {
    YamlConfiguration cfg = new YamlConfiguration();
    cfg.options().pathSeparator('/');
    cfg.load(cfgFile);

    this.debug = cfg.getBoolean("debug", this.debug);
    this.broadcastFlag = cfg.getBoolean("broadcastVote", this.broadcastFlag);
    this.checkPlayer = cfg.getBoolean("checkPlayer", this.checkPlayer);
    this.confirmMsg = cfg.getString("messages/confirm", "Thanks {IGN}, for voting on {SERVICE}!");
    this.paymentMsg = cfg.getString("messages/payment", "{AMOUNT} has been added to your {ECONOMY} balance.");
    this.broadcastMsg = cfg.getString("messages/broadcast", "The server was voted for by {IGN}!");
    this.prefix = cfg.getString("currency/prefix", "");
    this.suffix = cfg.getString("currency/suffix", "");

    ConfigurationSection cs = cfg.getConfigurationSection("rewards");
    if (cs != null)
      for (String vsName : cs.getKeys(false)) {
        ConfigurationSection vsConfig = cs.getConfigurationSection(vsName);
        if (vsConfig != null) {
          vsName = vsName.toLowerCase();
          this.vsConfigs.put(vsName, new VoteService(vsName, vsConfig));
        }
      }
  }

  private void saveConfiguration()
  {
    String cfg = buildConfiguration();
    if (cfg == null) {
      return;
    }
    if ((!this.plugin.getDataFolder().exists()) && (!this.plugin.getDataFolder().mkdir())) {
      LOGGER.warning("Unable to create JCVaultListener data folder!");
    }
    File cfgFile = new File(this.plugin.getDataFolder(), "config.yml");
    try
    {
      FileWriter fos = new FileWriter(cfgFile);
      fos.write(cfg);
      fos.close();
    }
    catch (IOException ex) {
      LOGGER.warning("Unable to save configuration file!");
    }
  }

  private ConfigTemplate readTemplate(String tmplName)
  {
    StringBuilder sb = new StringBuilder(1024);
    String ls = System.getProperty("line.separator");
    try
    {
      BufferedReader fis = new BufferedReader(new InputStreamReader(this.plugin.getResource(tmplName)));

      String line = null;
      while ((line = fis.readLine()) != null) {
        sb.append(line);
        sb.append(ls);
      }
      fis.close();
      return new ConfigTemplate(sb.toString());
    }
    catch (IOException ex) {
      LOGGER.warning(new StringBuilder().append("Unable to read '").append(tmplName).append("' configuration template. Configuration file will not be saved!").toString());
    }
    return null;
  }

  private String buildConfiguration()
  {
    ConfigTemplate tmpl = readTemplate("config.tmpl");
    ConfigTemplate vsTmpl = readTemplate("config_vs.tmpl");
    if ((tmpl == null) || (vsTmpl == null)) {
      return null;
    }
    tmpl.replace("debug", new StringBuilder().append("").append(this.debug).toString());
    tmpl.replace("broadcastVote", new StringBuilder().append("").append(this.broadcastFlag).toString());
    tmpl.replace("checkPlayer", new StringBuilder().append("").append(this.checkPlayer).toString());
    tmpl.replace("messages/confirm", this.confirmMsg.replace("\n", "\\n"));
    tmpl.replace("messages/payment", this.paymentMsg.replace("\n", "\\n"));
    tmpl.replace("messages/broadcast", this.broadcastMsg.replace("\n", "\\n"));
    tmpl.replace("currency/prefix", this.prefix);
    tmpl.replace("currency/suffix", this.suffix);

    VoteService vs = getVoteService("default");
    tmpl = vs.buildConfiguration("default", tmpl);

    String configInfo = tmpl.toString();

    for (Map.Entry<String, VoteService> e : this.vsConfigs.entrySet()) {
      String vsName = (String)e.getKey();
      if (!vsName.equals("default")) {
        if (vsName.matches(".*\\s.*"))
          vsName = new StringBuilder().append("'").append(vsName).append("'").toString();
        vs = (VoteService)e.getValue();
        configInfo = new StringBuilder().append(configInfo).append(vs.buildConfiguration(vsName, vsTmpl).toString()).toString();
      }
    }
    return configInfo;
  }

  public boolean getCheckPlayer()
  {
    return this.checkPlayer;
  }

  public String getConfirmMsg()
  {
    return this.confirmMsg;
  }

  public String getPaymentMsg()
  {
    return this.paymentMsg;
  }

  public String getBroadcastMsg()
  {
    return this.broadcastMsg;
  }

  public boolean getBroadcastFlag()
  {
    return this.broadcastFlag;
  }

  public String getPrefix()
  {
    return this.prefix;
  }

  public String getSuffix()
  {
    return this.suffix;
  }

  public boolean isDebug()
  {
    return this.debug;
  }

  public void dumpConfiguration()
  {
    LOGGER.info("[DBG] ===== JCVaultListener Configuration Dump =====");
    LOGGER.info(new StringBuilder().append("[DBG] debug: ").append(this.debug).toString());
    LOGGER.info(new StringBuilder().append("[DBG] checkPlayer: ").append(this.checkPlayer).toString());
    LOGGER.info(new StringBuilder().append("[DBG] broadcastVote: ").append(this.broadcastFlag).toString());
    LOGGER.info("[DBG] messages:");
    LOGGER.info(new StringBuilder().append("[DBG]   confirm: ").append(this.confirmMsg).toString());
    LOGGER.info(new StringBuilder().append("[DBG]   payment: ").append(this.paymentMsg).toString());
    LOGGER.info(new StringBuilder().append("[DBG]   broadcast: ").append(this.broadcastMsg).toString());
    LOGGER.info("[DBG] currency:");
    LOGGER.info(new StringBuilder().append("[DBG]   prefix: ").append(this.prefix).toString());
    LOGGER.info(new StringBuilder().append("[DBG]   suffix: ").append(this.suffix).toString());
    LOGGER.info("[DBG] rewards:");
    for (VoteService vs : this.vsConfigs.values())
      vs.dumpConfiguration();
  }
}
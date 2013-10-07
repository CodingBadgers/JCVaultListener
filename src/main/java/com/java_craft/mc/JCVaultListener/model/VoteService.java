package com.java_craft.mc.JCVaultListener.model;

import com.java_craft.mc.JCVaultListener.utils.ConfigTemplate;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

public class VoteService
  implements IVaultListenerConstants
{
  private static final Logger LOGGER = Logger.getLogger("JCVaultListener");
  private String name;
  private double amount;
  private double rate;
  private PayType type;

  public VoteService(String name)
  {
    this.name = name;
    this.amount = 30.0D;
    this.rate = 0.01D;
    this.type = PayType.valueOf("FIXED");
  }

  public VoteService(String name, double amount, double rate, PayType type)
  {
    this.name = name;
    this.amount = amount;
    this.rate = rate;
    this.type = type;
  }

  public VoteService(String name, ConfigurationSection cs)
  {
    this.name = name;
    this.amount = cs.getDouble("amount", 30.0D);
    this.rate = cs.getDouble("rate", 0.01D);

    String strType = cs.getString("type", "FIXED").toUpperCase();
    try {
      this.type = PayType.valueOf(strType);
    }
    catch (IllegalArgumentException ex) {
      LOGGER.warning("'" + strType + "' is not a valid reward type. Using 'fixed'");
      this.type = PayType.FIXED;
    }
  }

  public double calculatePay(double balance, String ign, boolean debug)
  {
    double paid = 0.0D;

    switch (this.type.ordinal())
    {
    case 1:
      if (debug) {
        LOGGER.info("[DBG] " + ign + " balance (if 0.00, player may not have economy account): " + balance);
      }

      paid = balance * this.rate;
      if (debug) {
        LOGGER.info("[DBG] Calculated reward: " + paid);
      }
      if (paid < this.amount) {
        paid = this.amount;
        if (debug)
          LOGGER.info("[DBG] Calculated reward less than fixed amount. Paying fixed amount: " + paid);  } break;
    default:
      paid = this.amount;
      if (debug)
        LOGGER.info("[DBG] Paying fixed amount: " + paid);
      break;
    }
    paid = Math.round(100.0D * paid) / 100.0D;
    return paid;
  }

  public ConfigTemplate buildConfiguration(String name, ConfigTemplate tmpl)
  {
    ConfigTemplate tmpl1 = new ConfigTemplate(tmpl);
    tmpl1.replace("vsName", name);
    tmpl1.replace("type", this.type.toString());
    tmpl1.replace("amount", Double.toString(this.amount));
    tmpl1.replace("rate", Double.toString(this.rate));

    return tmpl1;
  }

  public void dumpConfiguration()
  {
    LOGGER.info("[DBG]   " + this.name + ":");
    LOGGER.info("[DBG]    amount: " + this.amount);
    LOGGER.info("[DBG]    rate: " + this.rate);
    LOGGER.info("[DBG]    type: " + this.type.toString());
  }
}
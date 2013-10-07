package com.java_craft.mc.JCVaultListener.listeners;

import com.java_craft.mc.JCVaultListener.JCVaultListener;
import com.java_craft.mc.JCVaultListener.config.ConfigurationSet;
import com.java_craft.mc.JCVaultListener.model.VoteService;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import java.util.HashMap;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class VotifierListener
  implements Listener
{
  private static final Logger LOGGER = Logger.getLogger("JCVaultListener");
  private final JCVaultListener plugin;
  private ConfigurationSet cfg;
  private double paid = 0.0D;
  
  private HashMap<String, Long> m_antiSpamMap = new HashMap<String, Long>();

  private boolean debug = false;

  public VotifierListener(JCVaultListener plugin)
  {
    this.plugin = plugin;
    this.cfg = plugin.getConfiguration();
    this.debug = this.cfg.isDebug();
  }

  @EventHandler(priority=EventPriority.NORMAL)
  public void onVotifierEvent(VotifierEvent event)
  {
    Vote vote = event.getVote();
    processVote(vote);
  }

  public void processVote(Vote vote)
  {
    String ign = vote.getUsername();
    if ((this.cfg.getCheckPlayer()) && (!verifyPlayer(ign))) {
      LOGGER.info("Ignoring vote received by unrecognized player '" + ign + "'");
      return;
    }

    if (this.cfg.isDebug()) {
      LOGGER.info("[DBG] Votifier record: " + vote);
      this.cfg.dumpConfiguration();
    }

    final String serviceName = vote.getServiceName();
    boolean broadcastMessage = true;
    
    if (m_antiSpamMap.containsKey(serviceName)) {
    	long lastTime = m_antiSpamMap.get(serviceName);
    	if (System.currentTimeMillis() - lastTime < this.cfg.getAntiSpamTimeMilliseconds()) {
    		broadcastMessage = false;
    	}
    	else {
    		m_antiSpamMap.remove(serviceName);
    		m_antiSpamMap.put(serviceName, System.currentTimeMillis());
    	}
    }
    else {
    	m_antiSpamMap.put(serviceName, System.currentTimeMillis());
    }
    
    VoteService vs = this.cfg.getVoteService(serviceName);
    
    Economy econ = this.plugin.getEconomy();

    if (econ != null) {
      if (this.debug) {
        LOGGER.info("[DBG] Using " + econ.getName() + " to pay IGN -> " + ign);
      }
      double balance = econ.getBalance(ign);
      this.paid = vs.calculatePay(balance, ign, this.cfg.isDebug());

      EconomyResponse eres = econ.depositPlayer(ign, this.paid);
      if (eres.type == EconomyResponse.ResponseType.FAILURE)
        LOGGER.info(eres.errorMessage);
    }
    else {
      this.paid = 0.0D;
      if (this.debug) {
        LOGGER.info("[DBG] No economy plugin found");
      }
    }
    Player player = this.plugin.getServer().getPlayerExact(ign);

    if (player != null) {
      sendMessage(player, vote, this.cfg.getConfirmMsg(), "Confirmation");

      if (econ != null) {
        sendMessage(player, vote, this.cfg.getPaymentMsg(), "Payment");
      }
      else if (this.debug)
        LOGGER.info("[DBG] No economy plugin found. No payment message sent.");
    }
    else if (this.debug) {
      LOGGER.info("[DBG] No online player found for -> " + ign);
    }
    if (this.cfg.getBroadcastFlag() && broadcastMessage == true)
      broadcastMessage(vote, this.cfg.getBroadcastMsg());
    else if (this.debug)
      LOGGER.info("[DBG] Broadcast disabled. No broadcast message sent.");
  }

  private boolean verifyPlayer(String ign)
  {
    OfflinePlayer validPlayer = null;

    for (OfflinePlayer offPlayer : Bukkit.getOfflinePlayers()) {
      if (offPlayer.getName().toLowerCase().equals(ign.toLowerCase())) {
        validPlayer = offPlayer;
        break;
      }
    }

    return validPlayer != null;
  }

  private void sendMessage(Player player, Vote vote, String msg, String dbgId)
  {
    for (String s : insertTokenData(vote, msg)) {
      player.sendMessage(s);
      if (this.debug)
        LOGGER.info("[DBG] " + dbgId + " message -> " + s);
    }
  }

  private void broadcastMessage(Vote vote, String msg)
  {
    Server server = this.plugin.getServer();

    for (String s : insertTokenData(vote, msg)) {
      server.broadcastMessage(s);
      if (this.debug)
        LOGGER.info("[DBG] Broadcast message -> " + s);
    }
  }

  private String[] insertTokenData(Vote vote, String str)
  {
    String msg = str.replace("{SERVICE}", vote.getServiceName());
    msg = msg.replace("{IGN}", vote.getUsername());
    msg = msg.replace("{AMOUNT}", this.cfg.getPrefix() + Double.toString(this.paid) + this.cfg.getSuffix());
    msg = msg.replace("{ECONOMY}", this.plugin.getEconomy() != null ? this.plugin.getEconomy().getName() : "UNKNOWN");

    msg = msg.replaceAll("(?i)&([0-9A-FK-OR])", "§$1");
    return msg.split("\n");
  }
}
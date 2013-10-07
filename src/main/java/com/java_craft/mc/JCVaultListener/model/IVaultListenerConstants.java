package com.java_craft.mc.JCVaultListener.model;

public abstract interface IVaultListenerConstants
{
  public static final String D_VS_AMOUNT = "amount";
  public static final String D_VS_RATE = "rate";
  public static final String D_VS_TYPE = "type";
  public static final String D_VS_NAME = "vsName";
  public static final double DEF_VS_AMOUNT = 30.0D;
  public static final double DEF_VS_RATE = 0.01D;
  public static final String DEF_VS_TYPE = "FIXED";
  public static final String D_DEBUG = "debug";
  public static final String D_BROADCAST_VOTE = "broadcastVote";
  public static final String D_CHECK_PLAYER = "checkPlayer";
  public static final String D_CONFIRM_MSG = "messages/confirm";
  public static final String D_PAYMENT_MSG = "messages/payment";
  public static final String D_BROADCAST_MSG = "messages/broadcast";
  public static final String D_PREFIX = "currency/prefix";
  public static final String D_SUFFIX = "currency/suffix";
  public static final String DEF_CONFIRM_MSG = "Thanks {IGN}, for voting on {SERVICE}!";
  public static final String DEF_PAYMENT_MSG = "{AMOUNT} has been added to your {ECONOMY} balance.";
  public static final String DEF_BROADCAST_MSG = "The server was voted for by {IGN}!";
  public static final String DEF_PREFIX = "";
  public static final String DEF_SUFFIX = "";
  public static final String DEF_VOTESERVICE = "default";
  public static final String CFG_VS_TEMPLATE = "config_vs.tmpl";
  public static final String CFG_TEMPLATE = "config.tmpl";
  public static final String CFG_FILENAME = "config.yml";
}
package com.java_craft.mc.JCVaultListener.utils;

public class ConfigTemplate
{
  private String config;

  public ConfigTemplate(String config)
  {
    this.config = config;
  }

  public ConfigTemplate(ConfigTemplate tmpl)
  {
    this.config = new String(tmpl.config);
  }

  public void replace(String tag, String value)
  {
    this.config = this.config.replace("%" + tag + "%", value);
  }

  public String toString()
  {
    return this.config;
  }
}
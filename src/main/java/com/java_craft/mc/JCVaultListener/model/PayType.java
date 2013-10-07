package com.java_craft.mc.JCVaultListener.model;

public enum PayType
{
  FIXED, 

  RATE;

  public String toString()
  {
    return super.toString().toLowerCase();
  }
}
package com.java_craft.mc.JCVaultListener.logging;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class LogFilter
  implements Filter
{
  private String prefix;

  public LogFilter(String prefix)
  {
    this.prefix = prefix;
  }

  public boolean isLoggable(LogRecord record)
  {
    record.setMessage(this.prefix + record.getMessage());
    return true;
  }
}
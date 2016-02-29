package io.netlibs.fluctuate;

public class StringUtils
{

  private static final String EMPTY = "";

  public static String trimToEmpty(String str)
  {
    return str == null ? EMPTY : str.trim();
  }

}

package io.netlibs.fluctuate;

import java.net.InetSocketAddress;

/**
 * primary configuration and management point for fluctuate.
 * 
 * @author Theo Zourzouvillys
 *
 */

public final class Fluctuate
{

  private Fluctuate()
  {
    // don't allow instantiation.
  }

  /**
   * Returns a new HTTP(S) listener that is running on an ephemeral port.
   */

  public static FluctuateHttpListenerBuilder newListener()
  {
    return newListener(0);
  }

  public static FluctuateHttpListenerBuilder newListener(int port)
  {
    return newListener(new InetSocketAddress(port));
  }

  public static FluctuateHttpListenerBuilder newListener(InetSocketAddress listen)
  {
    return new FluctuateHttpListenerBuilder(listen);
  }


}

package io.netlibs.fluctuate;

import io.netlibs.fluctuate.tls.HttpListener;

public class FluctuateHttpListener implements FluctuateListener
{

  private HttpListener listener;

  public FluctuateHttpListener(HttpListener listener)
  {
    this.listener = listener;
  }

}

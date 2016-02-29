package io.netlibs.fluctuate;

import io.netlibs.fluctuate.tls.TlsListener;

public class FluctuateTlsListener implements FluctuateListener
{

  private TlsListener listener;

  public FluctuateTlsListener(TlsListener listener)
  {
    this.listener = listener;
  }

}

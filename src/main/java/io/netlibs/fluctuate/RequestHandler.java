package io.netlibs.fluctuate;

import io.netlibs.fluctuate.jersey2.HttpTxnHandle;

@FunctionalInterface
public interface RequestHandler
{
  
  void process(HttpTxnHandle req);

}

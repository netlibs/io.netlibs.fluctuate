package io.netlibs.fluctuate;

import io.netty.handler.ssl.SslContext;

/**
 * Provides the interface for calculating where a websocket should be forwarded, based upon the incoming connection.
 * 
 * The target could be a local processor, which then demuxes on a per frame basis, if that was so desired.
 * 
 * @author theo
 *
 */

@FunctionalInterface
public interface WebRouter
{

  /**
   * Called when there is a new incoming connection. Returns the handle to be used to service events on it.
   * 
   * @param hostname
   * @param ssl
   * @return
   */

  public Connection connection(String hostname, SslContext ssl);

}

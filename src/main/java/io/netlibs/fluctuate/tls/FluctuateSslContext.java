package io.netlibs.fluctuate.tls;

import java.util.List;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolNegotiator;
import io.netty.handler.ssl.OpenSslEngine;
import io.netty.handler.ssl.SslContext;

public class FluctuateSslContext extends SslContext
{

  private SslContext wrapped;

  public FluctuateSslContext(SslContext wrapped)
  {
    this.wrapped = wrapped;
  }

  @Override
  public boolean isClient()
  {
    return wrapped.isClient();
  }

  @Override
  public List<String> cipherSuites()
  {
    return wrapped.cipherSuites();
  }

  @Override
  public long sessionCacheSize()
  {
    return wrapped.sessionCacheSize();
  }

  @Override
  public long sessionTimeout()
  {
    return wrapped.sessionTimeout();
  }

  @Override
  public ApplicationProtocolNegotiator applicationProtocolNegotiator()
  {
    return wrapped.applicationProtocolNegotiator();
  }

  @Override
  public SSLEngine newEngine(ByteBufAllocator alloc)
  {
    return configure(wrapped.newEngine(alloc));
  }

  @Override
  public SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort)
  {
    return configure(wrapped.newEngine(alloc, peerHost, peerPort));
  }

  private SSLEngine configure(SSLEngine engine)
  {
    OpenSslEngine ssl = (OpenSslEngine)engine;
    // System.err.println(Arrays.asList(engine.getSupportedProtocols()));
    engine.setEnabledProtocols(new String[] { "TLSv1.1", "TLSv1.2" });
    return engine;
  }

  @Override
  public SSLSessionContext sessionContext()
  {
    return wrapped.sessionContext();
  }

}

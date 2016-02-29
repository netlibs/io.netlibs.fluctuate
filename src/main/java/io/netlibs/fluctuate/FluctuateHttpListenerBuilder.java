package io.netlibs.fluctuate;

import java.net.InetSocketAddress;
import java.util.function.Function;

import io.netlibs.fluctuate.tls.HttpListener;
import io.netlibs.fluctuate.tls.TlsKeyingData;
import io.netlibs.fluctuate.tls.TlsListener;
import io.netty.channel.EventLoopGroup;

public class FluctuateHttpListenerBuilder
{

  private InetSocketAddress listen;
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private Function<String, TlsKeyingData> tlsResolver;

  public FluctuateHttpListenerBuilder(InetSocketAddress listen)
  {
    this.listen = listen;
  }

  public FluctuateHttpListenerBuilder tlsResolver(Function<String, TlsKeyingData> resolver)
  {
    this.tlsResolver = resolver;
    return this;
  }

  public FluctuateHttpListenerBuilder group(EventLoopGroup bossGroup, EventLoopGroup workerGroup)
  {
    this.bossGroup = bossGroup;
    this.workerGroup = workerGroup;
    return this;
  }

  public FluctuateListener start(WebRouter handler)
  {
    if (this.tlsResolver != null)
    {
      TlsListener tls = new TlsListener(bossGroup, workerGroup);
      tls.listen(listen, tlsResolver, handler);
      return new FluctuateTlsListener(tls);
    }
    else
    {
      HttpListener http = new HttpListener(bossGroup, workerGroup);
      http.listen(listen, handler);
      return new FluctuateHttpListener(http);
    }
  }

}

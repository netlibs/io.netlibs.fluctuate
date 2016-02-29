package io.netlibs.fluctuate.tls;

import java.net.InetSocketAddress;
import java.util.function.Function;

import io.netlibs.fluctuate.WebRouter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class TlsListener
{

  private final EventLoopGroup bossGroup;
  private final EventLoopGroup workerGroup;
  Channel server;

  public TlsListener(EventLoopGroup bossGroup, EventLoopGroup workerGroup)
  {
    this.bossGroup = bossGroup;
    this.workerGroup = workerGroup;
  }

  public void listen(int port, Function<String, TlsKeyingData> tls, WebRouter router)
  {
    listen(new InetSocketAddress(port), tls, router);
  }

  public void listen(InetSocketAddress listen, Function<String, TlsKeyingData> tls, WebRouter router)
  {
    final ServerBootstrap b = new ServerBootstrap();
    b.option(ChannelOption.SO_BACKLOG, 1024);
    b.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new TlsChannelInitializer(router, tls));
    try
    {
      this.server = b.bind(listen).sync().channel();
    }
    catch (InterruptedException ex)
    {
      throw new RuntimeException(ex);
    }

  }

}

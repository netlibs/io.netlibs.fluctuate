package io.netlibs.fluctuate.tls;

import io.netlibs.fluctuate.WebRouter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpOnlyChannelInitializer extends ChannelInitializer<SocketChannel>
{

  private static final int MAX_CONTENT_LENGTH = 1024 * 100;
  private WebRouter router;

  public HttpOnlyChannelInitializer(WebRouter router)
  {
    this.router = router;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception
  {

    final ChannelPipeline pipeline = ch.pipeline();

    pipeline.addLast(
        new HttpServerCodec(),
        new HttpObjectAggregator(MAX_CONTENT_LENGTH),
        new Http1RequestHandler(router.connection(null, null)));

  }

}

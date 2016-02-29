package io.netlibs.fluctuate.tls;

import io.netlibs.fluctuate.Connection;
import io.netlibs.fluctuate.WebRouter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * performs the relevant check on TLS negotiation, to work out which protocol (currently HTTP/1.1 or 2/.0) that the client wants.
 * 
 * @author Theo Zourzouvillys
 *
 */

@Slf4j
class Http2OrHttpHandler extends ChannelInboundHandlerAdapter
{

  private static final int MAX_CONTENT_LENGTH = 1024 * 100;
  private SniHandler sni;
  private WebRouter router;

  protected Http2OrHttpHandler(WebRouter router, SniHandler handler)
  {
    this.router = router;
    this.sni = handler;
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
  {
    if (evt instanceof SslHandshakeCompletionEvent)
    {
      final SslHandler sslHandler = ctx.pipeline().get(SslHandler.class);
      ctx.pipeline().remove(this);
      SslHandshakeCompletionEvent handshakeEvent = (SslHandshakeCompletionEvent) evt;
      if (handshakeEvent.isSuccess())
      {
        String protocol = sslHandler.applicationProtocol();
        configurePipeline(ctx, protocol != null ? protocol : ApplicationProtocolNames.HTTP_1_1);
      }
      else
      {
        handshakeFailure(ctx, handshakeEvent.cause());
      }
    }
    ctx.fireUserEventTriggered(evt);
  }

  /**
   * This is where we decide what to do next.
   * 
   * @param ctx
   * @param protocol
   * @throws Exception
   */

  protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception
  {

    if (ApplicationProtocolNames.HTTP_2.equals(protocol))
    {
      log.debug("Client requested host {}, negotiated HTTP/2.0", sni.hostname());
      configureHttp2(ctx, router.connection(sni.hostname(), sni.sslContext()));
    }
    else if (ApplicationProtocolNames.HTTP_1_1.equals(protocol))
    {
      log.debug("HTTP/1.1 client requesting {}", sni.hostname());
      configureHttp1(ctx, router.connection(sni.hostname(), sni.sslContext()));
    }
    else
    {
      log.warn("Unknown ALPN protocol: {}", protocol);
      throw new IllegalStateException("unknown protocol: " + protocol);
    }

  }

  /**
   * Configure this connection for HTTP/1.1.
   */

  private void configureHttp1(ChannelHandlerContext ctx, Connection conn)
  {
    ctx.pipeline().addLast(
        new HttpServerCodec(),
        new HttpObjectAggregator(MAX_CONTENT_LENGTH),
        new Http1RequestHandler(conn));
  }

  /**
   * Configure this connection to have HTTP/2.0 semantics.
   */

  private void configureHttp2(ChannelHandlerContext ctx, Connection conn)
  {

    DefaultHttp2Connection connection = new DefaultHttp2Connection(true);

    InboundHttp2ToHttpAdapter listener = new InboundHttp2ToHttpAdapterBuilder(connection)
        .propagateSettings(true).validateHttpHeaders(false)
        .maxContentLength(MAX_CONTENT_LENGTH).build();

    ctx.pipeline().addLast(new HttpToHttp2ConnectionHandlerBuilder()
        .frameListener(listener)
        .connection(connection).build());

    ctx.pipeline().addLast(new Http2RequestHandler(conn));

  }

  /**
   * Invoked on failed initial SSL/TLS handshake.
   */

  protected void handshakeFailure(ChannelHandlerContext ctx, Throwable cause) throws Exception
  {
    log.warn("{} TLS handshake failed:", ctx.channel(), cause);
    ctx.close();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
  {
    log.warn("{} Failed to select the application-level protocol:", ctx.channel(), cause);
    ctx.close();
  }

}

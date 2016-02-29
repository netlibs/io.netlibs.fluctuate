package io.netlibs.fluctuate.tls;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.netlibs.fluctuate.Connection;
import io.netlibs.fluctuate.http.Http1RequestProxy;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles HTTP/1.1 connections.
 * 
 * HTTP/1.1 can include an upgrade to websockets, in which case we handle it accordingly by switching to become a websocket connection. This
 * doesn't actually involve us answering directly, but instead passing to wherever we want to proxy to.
 * 
 * Note that HTTP/1.1 pipelining may be used, so we need to be careful about ordering and HOL blocking.
 * 
 */

@Slf4j
public final class Http1RequestHandler extends SimpleChannelInboundHandler<FullHttpRequest>
{

  private Connection conn;

  public Http1RequestHandler(Connection conn)
  {
    this.conn = conn;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception
  {

    // now, work out what's going to actually handle the request.

    String upgrade = req.headers().get(HttpHeaderNames.SEC_WEBSOCKET_VERSION);

    if (upgrade != null)
    {
      switchToWebSocket(ctx, req);
      return;
    }

    if (HttpUtil.is100ContinueExpected(req))
    {

      // TODO: change to actually pass forward rather than buffer.
      ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));

    }

    // it's not a websocket upgrade, so presume it's a basic HTTP request.
    // all requests get forwarded to the connection processor.
    conn.request(new Http1RequestProxy(ctx, req));

  }

  private void switchToWebSocket(ChannelHandlerContext ctx, HttpRequest req)
  {

    // Allow only GET methods with a WS upgrade.
    if (req.method() != GET)
    {
      log.warn("Can't use non-GET method to upgrade to websocket");
      Http1Utils.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
      return;
    }

    conn.upgrade(ctx, req);

  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
  {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
  {
    log.warn("An exception was caught", cause);
    ctx.close();
    this.conn.closed();
  }

}
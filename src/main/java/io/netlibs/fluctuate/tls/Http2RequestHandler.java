package io.netlibs.fluctuate.tls;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.unmodifiableBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.util.CharsetUtil.UTF_8;

import io.netlibs.fluctuate.Connection;
import io.netlibs.fluctuate.http.Http2RequestProxy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;

/**
 * Handles all the requests for data. It receives a {@link FullHttpRequest}, which has been converted by a {@link InboundHttp2ToHttpAdapter}
 * before it arrived here. For further details, check {@link Http2OrHttpHandler} where the pipeline is setup.
 */

public class Http2RequestHandler extends SimpleChannelInboundHandler<FullHttpRequest>
{

  private static final ByteBuf response = unmodifiableBuffer(unreleasableBuffer(copiedBuffer("<!DOCTYPE html><html><body><h2>Hello</h2></body></html>", UTF_8)));
  private Connection conn;

  public Http2RequestHandler(Connection conn)
  {
    this.conn = conn;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception
  {
    conn.request(new Http2RequestProxy(ctx, request));
  }

}
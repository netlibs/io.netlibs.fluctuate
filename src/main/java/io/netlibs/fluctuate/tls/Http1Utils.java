package io.netlibs.fluctuate.tls;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

public class Http1Utils
{


  public static void sendHttpResponse(final ChannelHandlerContext ctx, final HttpRequest req, final FullHttpResponse res)
  {

    // Generate an error page if response getStatus code is not OK (200).
    if (res.status().code() != 200)
    {
      final ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
      res.content().writeBytes(buf);
      buf.release();
      HttpUtil.setContentLength(res, res.content().readableBytes());
    }

    // Send the response and close the connection if necessary.
    final ChannelFuture f = ctx.channel().writeAndFlush(res);

    if (!HttpUtil.isKeepAlive(req) || (res.status().code() != 200))
    {
      f.addListener(ChannelFutureListener.CLOSE);
    }

  }


}

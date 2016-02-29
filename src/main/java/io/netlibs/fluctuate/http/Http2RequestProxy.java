package io.netlibs.fluctuate.http;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.HttpConversionUtil;

public class Http2RequestProxy extends AbstractRequestProxy
{

  public Http2RequestProxy(ChannelHandlerContext ctx, FullHttpRequest req)
  {
    super(ctx, req);
  }

  @Override
  public void reject(int status, String reason)
  {
    DefaultFullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), new HttpResponseStatus(status, reason));
    res.headers().set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), req.headers().get(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text()));
    ctx.writeAndFlush(res);
  }

  @Override
  public void respond(String data)
  {
    DefaultFullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK, Unpooled.wrappedBuffer(data.getBytes(StandardCharsets.UTF_8)));
    res.headers().set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), req.headers().get(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text()));
    ctx.writeAndFlush(res);
  }


  @Override
  public void send(FullHttpResponse res)
  {
    res.headers().set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), req.headers().get(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text()));
    ctx.writeAndFlush(res);
  }

}

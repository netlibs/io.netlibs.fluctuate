package io.netlibs.fluctuate.http;

import java.nio.charset.StandardCharsets;

import io.netlibs.fluctuate.tls.Http1Utils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class Http1RequestProxy extends AbstractRequestProxy
{

  public Http1RequestProxy(ChannelHandlerContext ctx, FullHttpRequest req)
  {
    super(ctx, req);
  }

  @Override
  public void reject(int status, String reason)
  {
    Http1Utils.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(req.protocolVersion(), new HttpResponseStatus(status, reason)));
  }

  @Override
  public void respond(String data)
  {
    DefaultFullHttpResponse res = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK, Unpooled.wrappedBuffer(data.getBytes(StandardCharsets.UTF_8)));
    Http1Utils.sendHttpResponse(ctx, req, res);
  }

  @Override
  public void send(FullHttpResponse res)
  {
    Http1Utils.sendHttpResponse(ctx, req, res);
  }

}

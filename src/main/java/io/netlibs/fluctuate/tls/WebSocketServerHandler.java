package io.netlibs.fluctuate.tls;

import io.netlibs.fluctuate.Connection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler put into the pipeline after a websocket upgrade.
 * 
 * @author theo
 *
 */

@Slf4j
public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame>
{

  private Connection conn;
  private WebSocketServerHandshaker handshaker;

  public WebSocketServerHandler(Connection conn, WebSocketServerHandshaker handshaker, String uri, HttpHeaders headers)
  {
    this.conn = conn;
    this.handshaker = handshaker;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception
  {
    if (frame instanceof CloseWebSocketFrame)
    {
      handle(ctx, (CloseWebSocketFrame) frame);
    }
    else if (frame instanceof PingWebSocketFrame)
    {
      handle(ctx, (PingWebSocketFrame) frame);
    }
    else if (frame instanceof PongWebSocketFrame)
    {
      handle(ctx, (PongWebSocketFrame) frame);
    }
    else if (frame instanceof ContinuationWebSocketFrame)
    {
      handle(ctx, (ContinuationWebSocketFrame) frame);
    }
    else if (frame instanceof BinaryWebSocketFrame)
    {
      handle(ctx, (BinaryWebSocketFrame) frame);
    }
    else if (frame instanceof TextWebSocketFrame)
    {
      handle(ctx, (TextWebSocketFrame) frame);
    }
    else
    {
      throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
    }
  }

  protected void handle(ChannelHandlerContext ctx, CloseWebSocketFrame frame) throws Exception
  {
    log.debug("incoming close frame");
    this.handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
  }

  protected void handle(ChannelHandlerContext ctx, PingWebSocketFrame frame) throws Exception
  {
    log.debug("incoming ping frame");
    ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
  }

  protected void handle(ChannelHandlerContext ctx, PongWebSocketFrame frame) throws Exception
  {
    log.debug("incoming pong frame");
  }

  protected void handle(ChannelHandlerContext ctx, ContinuationWebSocketFrame frame) throws Exception
  {
    log.debug("incoming continuation frame");
  }

  protected void handle(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception
  {
    log.debug("incoming binary frame");
  }

  protected void handle(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception
  {
    log.debug("incoming text frame");
  }

}

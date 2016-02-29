package io.netlibs.fluctuate.http;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import io.netlibs.fluctuate.jersey2.HttpTxnHandle;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.concurrent.ScheduledFuture;

public abstract class AbstractRequestProxy implements HttpTxnHandle
{

  protected ChannelHandlerContext ctx;
  protected FullHttpRequest req;
  private QueryStringDecoder decoder;
  private Map<String, List<String>> entries;

  public AbstractRequestProxy(ChannelHandlerContext ctx, FullHttpRequest req)
  {
    this.ctx = ctx;
    this.req = req;
    this.decoder = new QueryStringDecoder(req.uri());

    this.entries = new HashMap<>();

    for (Entry<String, String> e : req.headers())
    {
      this.entries.computeIfAbsent(e.getKey(), (val) -> new ArrayList<String>()).add(e.getValue());

    }

  }

  @Override
  public String method()
  {
    return req.method().toString();
  }

 
  @Override
  public Map<String, List<String>> headers()
  {
    return entries;
  }

  @Override
  public String uri()
  {
    return decoder.uri();
  }

  @Override
  public Map<String, List<String>> query()
  {
    return decoder.parameters();
  }

  @Override
  public String path()
  {
    return decoder.path();
  }

  @Override
  public String host()
  {
    String value = req.headers().getAllAsString("host").get(0);
    return value;
  }

  @Override
  public String cookie(String name)
  {
    for (String cookies : req.headers().getAllAsString("cookie"))
    {
      for (Cookie cookie : ServerCookieDecoder.LAX.decode(cookies))
      {
        if (!cookie.name().startsWith(name + "="))
        {
          continue;
        }
        return cookie.value();
      }
    }
    return null;
  }

  @Override
  public InputStream input()
  {
    return new ByteBufInputStream(req.content());
  }

  @Override
  public Runnable schedule(Duration timeout, Runnable callback)
  {
    ScheduledFuture<?> future = ctx.executor().schedule(callback, timeout.toMillis(), TimeUnit.MILLISECONDS);
    return () -> future.cancel(false);
  }

}

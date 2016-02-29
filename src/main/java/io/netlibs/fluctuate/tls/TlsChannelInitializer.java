package io.netlibs.fluctuate.tls;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import io.netlibs.fluctuate.StringUtils;
import io.netlibs.fluctuate.WebRouter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SniHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Listens for a connection and then uses ALPN for calculating what happened.
 * 
 * @author theo
 *
 */

@Slf4j
public class TlsChannelInitializer extends ChannelInitializer<SocketChannel>
{

  private static ApplicationProtocolConfig DEFAULT_APN = new ApplicationProtocolConfig(
      Protocol.ALPN,
      SelectorFailureBehavior.NO_ADVERTISE,
      SelectedListenerFailureBehavior.ACCEPT,
      ApplicationProtocolNames.HTTP_2,
      ApplicationProtocolNames.HTTP_1_1);

  private Function<String, TlsKeyingData> ssl;
  private WebRouter router;

  public TlsChannelInitializer(WebRouter router, final Function<String, TlsKeyingData> tls)
  {
    this.router = router;
    this.ssl = Objects.requireNonNull(tls);
  }

  @Override
  public void initChannel(final SocketChannel ch) throws Exception
  {

    final ChannelPipeline pipeline = ch.pipeline();

    SniHandler handler = new SniHandler((input, promise) -> {
      log.debug("SNI lookup: [{}]", input);
      promise.setSuccess(lookup(input));
      return promise;
    });

    pipeline.addLast(handler);

    pipeline.addLast(new Http2OrHttpHandler(router, handler));

  }

  private Map<String, SslContext> contexts = new HashMap<>();

  @SneakyThrows
  private SslContext lookup(String input)
  {

    input = StringUtils.trimToEmpty(input).toLowerCase();

    SslContext cached = contexts.get(input);

    if (cached == null)
    {

      TlsKeyingData keying = ssl.apply(input);

      cached = SslContextBuilder
          .forServer(keying.getPrivateKey(), keying.getPublicKey())
          .applicationProtocolConfig(DEFAULT_APN)
          .build();

      if (cached == null)
      {
        return null;
      }

      contexts.put(StringUtils.trimToEmpty(keying.getServerName()), cached);

    }

    return cached;

  }

}

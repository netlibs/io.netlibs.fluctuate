package io.netlibs.fluctuate;

import io.netlibs.fluctuate.jersey2.HttpTxnHandle;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

public interface Connection
{

  void request(HttpTxnHandle req);

  void upgrade(ChannelHandlerContext ctx, HttpRequest req);

  void closed();

}

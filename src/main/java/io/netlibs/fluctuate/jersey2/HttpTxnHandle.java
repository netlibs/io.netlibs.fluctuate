package io.netlibs.fluctuate.jersey2;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.FullHttpResponse;

/**
 * To support both HTTP/1.1 and HTTP/2.0, provide an interface which hides away the underlying handler - HTTP/2.0 has some specifics around
 * streams which we need to handle seperately.
 * 
 * @author Theo Zourzouvillys
 *
 */

public interface HttpTxnHandle
{

  void respond(String data);

  void reject(int status, String reason);

  String path();

  default void reject(int code)
  {
    reject(code, "Unknown");
  }

  String host();

  void send(FullHttpResponse res);

  String cookie(String name);

  String method();

  String uri();

  Map<String, List<String>> query();

  InputStream input();

  Map<String, List<String>> headers();

  Runnable schedule(Duration timeout, Runnable callback);

}

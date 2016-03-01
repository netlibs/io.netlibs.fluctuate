package io.netlibs.fluctuate.jersey2;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ws.rs.core.Response.StatusType;

import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * An instance of this is passed into jersey as it's handle for sending the response.
 * 
 * @author theo
 *
 */

public class NettyContainerResponseWriter implements ContainerResponseWriter
{

  Logger log = Logger.getLogger(NettyContainerResponseWriter.class.getName());

  private class SuspensionData implements Runnable
  {

    private TimeoutHandler handler;
    public Runnable timeout;

    public SuspensionData(TimeoutHandler handler)
    {
      this.handler = handler;
    }

    public void cancel()
    {
      if (this.timeout != null)
      {
        this.timeout.run();
        this.timeout = null;
      }
    }

    // called when netty notifies us of timeout.
    @Override
    public void run()
    {
      this.timeout = null;
      TimeoutHandler handler = this.handler;
      this.handler = null;
      handler.onTimeout(NettyContainerResponseWriter.this);
    }

    public boolean update(Duration duration)
    {
      if (this.timeout != null)
      {
        this.cancel();
      }
      this.timeout = channel.schedule(duration, this);
      return true;
    }

  }

  private HttpTxnHandle channel;
  private DefaultFullHttpResponse response;
  private SuspensionData suspended = null;

  public NettyContainerResponseWriter(HttpTxnHandle req)
  {
    this.channel = Objects.requireNonNull(req);
  }

  /**
   * the netty http handlers deal with response buffering - either by actual buffering in http/1.1 without chunk support, or as there is no
   * need in HTTP/2.0. Either way, jersey can just send us data without needing to provide headers.
   */

  @Override
  public boolean enableResponseBuffering()
  {
    return false;
  }

  /**
   * the app is ready to send a response - or at least the HTTP headers and status.
   * 
   * @param contentLength
   *          The content length, if it's known. -1 if not.
   * 
   * @param responseContext
   *          The response context to send.
   * 
   */

  @Override
  public ByteBufOutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse responseContext) throws ContainerException
  {
    
    // create a full HTTP response.
    this.response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, makeStatus(responseContext.getStatusInfo()));

    // loop though each header and add to the netty response.
    for (Map.Entry<String, List<String>> e : responseContext.getStringHeaders().entrySet())
    {
      for (String value : e.getValue())
      {
        response.headers().add(e.getKey(), value);
      }
    }

    // the output stream adapter that writes to netty response.
    return new ByteBufOutputStream(response.content());

  }

  private HttpResponseStatus makeStatus(StatusType status)
  {
    if (status.getReasonPhrase() == null)
    {
      return HttpResponseStatus.valueOf(status.getStatusCode());
    }
    return new HttpResponseStatus(status.getStatusCode(), status.getReasonPhrase());
  }

  /**
   * the app is suspending it's request so it can do some async work. good for it!
   * 
   * only thing we need to do it set a timer.
   * 
   */

  @Override
  public boolean suspend(long amount, TimeUnit units, TimeoutHandler handler)
  {

    if (this.suspended != null)
    {
      // API says to return false if already set.
      return false;
    }

    this.suspended = new SuspensionData(handler);

    if (amount > 0)
    {
      this.suspended.update(Duration.of(units.toMillis(amount), ChronoUnit.MILLIS));
    }

    return true;
  }

  /**
   * called when the app updates it's timeout. we need to reschedule the timer.
   */

  @Override
  public void setSuspendTimeout(long amount, TimeUnit units) throws IllegalStateException
  {

    if (this.suspended == null)
    {
      throw new IllegalStateException("not suspended");
    }

    // update the timeout.
    this.suspended.update(Duration.of(units.toMillis(amount), ChronoUnit.MILLIS));

  }

  /**
   * application has completed. Send the response, and free ourselves.
   */

  @Override
  public void commit()
  {
    this.channel.send(response);
    this.cleanup();
  }

  /**
   * app threw an unhandled exception processing. we convert this into a 500 and generate some pretty error.
   * 
   * we must also clean up all resources (cancel timer, etc).
   * 
   */

  @Override
  public void failure(Throwable throwable)
  {
    log.severe(throwable.getMessage());
    this.channel.reject(500);
    this.cleanup();
  }

  private void cleanup()
  {
    if (this.suspended != null)
    {
      this.suspended.cancel();
      this.suspended = null;
    }
    this.channel = null;
  }

}

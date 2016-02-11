package io.netlibs.bgp.netty.simple;

import java.util.concurrent.atomic.AtomicInteger;

import io.netlibs.bgp.netty.protocol.BGPv4Packet;
import io.netlibs.bgp.netty.protocol.update.UpdatePacket;

/**
 * Handles the flow control for reading, ensuring we never read more than a certain number of packets.
 * 
 * @author theo
 *
 */

public class ReadTransferBuffer
{

  private static final int DEFAULT_INITIAL_CREDITS = 2500;

  /**
   * the number of packets.
   */

  private AtomicInteger credits;
  private Runnable read;
  private volatile boolean reading = false;
  private volatile boolean readable = false;

  private BGPv4SessionListener listener;

  public void init(Runnable read, BGPv4SessionListener listener)
  {
    this.credits = new AtomicInteger(DEFAULT_INITIAL_CREDITS);
    this.read = read;
    this.reading = false;
    this.readable = false;
    this.listener = listener;
  }

  public void enqueue(BGPv4Packet e)
  {

    // lower the credits before calling.
    this.credits.decrementAndGet();

    if (e instanceof UpdatePacket)
    {
      listener.update((UpdatePacket) e);
    }
    else
    {
      consume(1);
    }
  }

  /**
   * call to indicate that there is no longer a read operation on the socket side.
   */

  public void channelReadable()
  {

    this.reading = false;
    this.readable = true;

    triggerRead();

  }

  private boolean triggerRead()
  {
    if (!reading && readable && credits.get() > 0)
    {
      this.readable = false;
      this.reading = true;
      this.read.run();
      return true;
    }
    return false;
  }

  public void consume(int number)
  {
    this.credits.updateAndGet(current -> (current + number));
    triggerRead();
  }

}

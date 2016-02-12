package io.netlibs.bgp.netty.simple;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.netlibs.bgp.handlers.NotificationEvent;
import io.netlibs.bgp.netty.protocol.KeepalivePacket;
import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.bgp.netty.protocol.update.UpdatePacket;

public class ReadTransferBufferTest
{

  @Test
  public void test()
  {

    ReadTransferBuffer test = new ReadTransferBuffer();

    AtomicInteger reads = new AtomicInteger(0);
    AtomicInteger packets = new AtomicInteger(0);

    test.init(() -> reads.incrementAndGet(), new BGPv4SessionListener() {

      @Override
      public void update(UpdatePacket e)
      {
        packets.incrementAndGet();
      }

      @Override
      public void open(BGPv4Session session, OpenPacket remoteOpen)
      {
      }

      @Override
      public void notification(NotificationEvent e)
      {
      }

      @Override
      public void close()
      {
      }

    });

    // socket can read (but isn't). should trigger a read.
    test.channelReadable();

    assertEquals(1, reads.get());
    assertEquals(0, packets.get());

    // packet arrives from socket.  should immediately dispatch.
    test.enqueue(new UpdatePacket());

    assertEquals(1, reads.get());
    assertEquals(1, packets.get());

    // mark a credit.  shouldn't do anything, as we don't have a notification of read available.
    test.consume(1);
    assertEquals(1, reads.get());
    assertEquals(1, packets.get());

    // now socket is ready to read. should trigger a read.
    test.channelReadable();

    assertEquals(2, reads.get());
    assertEquals(1, packets.get());

    test.enqueue(new UpdatePacket());
    test.enqueue(new UpdatePacket());
    test.enqueue(new UpdatePacket());
    test.consume(3);

    assertEquals(2, reads.get());
    assertEquals(4, packets.get());

    test.channelReadable();

    assertEquals(3, reads.get());
    assertEquals(4, packets.get());

  }

}

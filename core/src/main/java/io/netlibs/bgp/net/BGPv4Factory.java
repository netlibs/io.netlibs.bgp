package io.netlibs.bgp.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.google.common.net.HostAndPort;

import io.netlibs.bgp.netty.simple.BGPv4Client;
import io.netlibs.bgp.netty.simple.BGPv4Server;
import io.netlibs.bgp.netty.simple.BGPv4SessionFactory;
import io.netlibs.bgp.netty.simple.BGPv4SessionListener;
import io.netlibs.bgp.netty.simple.LocalConfig;
import io.netlibs.bgp.protocol.BGPv4Constants;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * Primary API exposed to consumers who wish to use the BGPv4 library as a client or server.
 * 
 * Note that this is the "raw" service - it doesn't handle RIBs, announcements, etc. It purely works on the protocol itself, mapping
 * incoming and outgoing events and packets to a Java interface.
 * 
 * This means that at this layer there is no BGP state machine, bidirectional open detection, connection retries, etc. Use the higher level
 * implementations for that. The reasoning for this is that different BGP services have different requirements - building a full FSM doesn't
 * always make sense, for example in a passive listener or one which simply forwards the BGP packets to a message queue which does the FSM
 * behavior elsewhere.
 *
 * <code>
 * 
 * BGPv4Factory factory = new BGPv4Factory();
 * BGPv4Factory.listen(sessionFactory);
 * 
 * </code>
 * 
 * @author theo
 *
 */

public class BGPv4Factory
{

  private NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);

  /**
   * Listen on the specified {@link InetSocketAddress}.
   */

  public BGPv4Server listen(InetSocketAddress listen, BGPv4SessionFactory factory)
  {
    BGPv4Server server = new BGPv4Server(workerGroup, factory);
    server.start(listen);
    return server;
  }

  /**
   * Listen on the specified port on all IP addresses.
   */

  public BGPv4Server listen(int port, BGPv4SessionFactory factory)
  {
    return listen(new InetSocketAddress(port), factory);
  }

  /**
   * Listen on the specified IP address, on a random port.
   */

  public BGPv4Server listen(InetAddress address, BGPv4SessionFactory factory)
  {
    return listen(new InetSocketAddress(address, 0), factory);
  }

  /**
   * Listen on 0.0.0.0/::0, with a random port number.
   */

  public BGPv4Server listen(BGPv4SessionFactory factory)
  {
    return listen(new InetSocketAddress(0), factory);
  }

  /**
   * Tries to connect to the given {@link InetSocketAddress}.
   * 
   * If the connection fails, then the client will be unusable. instead, create a new one to perform retries (they're lightweight).
   * 
   */

  public BGPv4Client connect(InetSocketAddress target, BGPv4SessionListener listener, LocalConfig config)
  {

    HostAndPort remoteTarget = HostAndPort.fromParts(target.getAddress().getHostAddress(), target.getPort()).withDefaultPort(BGPv4Constants.BGP_PORT);

    BGPv4Client client = new BGPv4Client(workerGroup, remoteTarget, config);

    client.setListener(listener);
    client.connect();

    return client;

  }

}

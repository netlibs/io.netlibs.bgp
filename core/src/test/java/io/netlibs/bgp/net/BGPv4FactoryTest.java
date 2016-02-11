package io.netlibs.bgp.net;

import java.net.Inet4Address;

import org.junit.Test;

import io.netlibs.bgp.netty.simple.BGPv4Client;
import io.netlibs.bgp.netty.simple.BGPv4Server;
import io.netlibs.bgp.netty.simple.LocalConfig;
import io.netlibs.bgp.netty.simple.RemoteConfig;
import io.netlibs.ipaddr.IPv4Address;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BGPv4FactoryTest
{

  @Test
  public void test() throws Exception
  {

    BGPv4Factory factory = new BGPv4Factory();

    BGPv4Server server = factory.listen(Inet4Address.getLoopbackAddress(), (addr, open) -> {

      return RemoteConfig.builder()
          .localConfig(LocalConfig.builder()
              .autonomousSystem(1234)
              .bgpIdentifier(1111L)
              .holdTimeSeconds(15)
              .build())
          .listener(new TestSessionHandler())
          .build();

    });

    log.info("test BGP server Listening on {}", server.listenSocketAddress());

    // now, establish a connection

    BGPv4Client client = factory.connect(server.listenSocketAddress(), new TestSessionHandler(), LocalConfig.builder()
        .autonomousSystem(6643)
        .bgpIdentifier(IPv4Address.fromString("127.0.0.1").longValue())
        .holdTimeSeconds(30)
        .build());

    Thread.sleep(5000);

  }

}

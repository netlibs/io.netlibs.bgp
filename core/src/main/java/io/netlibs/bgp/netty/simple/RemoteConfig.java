package io.netlibs.bgp.netty.simple;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RemoteConfig
{
  private LocalConfig localConfig;
  private BGPv4SessionListener listener;
}

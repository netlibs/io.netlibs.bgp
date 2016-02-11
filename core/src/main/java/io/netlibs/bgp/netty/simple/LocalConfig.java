package io.netlibs.bgp.netty.simple;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LocalConfig
{
  private int autonomousSystem;
  private long bgpIdentifier;
  private int holdTimeSeconds;
}

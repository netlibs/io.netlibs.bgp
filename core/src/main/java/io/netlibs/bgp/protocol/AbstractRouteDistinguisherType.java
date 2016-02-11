package io.netlibs.bgp.protocol;

public interface AbstractRouteDistinguisherType 
{
  byte[] getBytes();
  byte[] getType();
  String humanReadable();
}
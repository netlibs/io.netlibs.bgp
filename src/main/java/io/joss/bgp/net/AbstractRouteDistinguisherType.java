package io.joss.bgp.net;

public interface AbstractRouteDistinguisherType 
{
  byte[] getBytes();
  byte[] getType();
  String humanReadable();
}
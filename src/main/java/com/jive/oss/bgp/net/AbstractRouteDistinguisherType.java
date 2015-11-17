package com.jive.oss.bgp.net;

public interface AbstractRouteDistinguisherType 
{
  byte[] getBytes();
  byte[] getType();
  String humanReadable();
}
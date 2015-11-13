package com.jive.oss.bgp.netty.service;

public interface AbstractRouteDistinguisherType 
{
  byte[] getBytes();
  
  byte[] getType();
}
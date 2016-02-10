package com.jive.oss.bgp.netty.service;

import org.junit.Test;

import com.google.common.primitives.Ints;

public class BGPv4ServiceTest
{

  @Test
  public void test()
  {
    int val = 32;
    val <<= 12;

    final byte[] to24 = Ints.toByteArray(val);


    System.out.println(Integer.toBinaryString((to24[2] & 0xFF) + 0x100).substring(1));
    System.out.println(Integer.toBinaryString((to24[1] & 0xFF) + 0x100).substring(1));
    System.out.println(Integer.toBinaryString((to24[0] & 0xFF) + 0x100).substring(1));

  }

}

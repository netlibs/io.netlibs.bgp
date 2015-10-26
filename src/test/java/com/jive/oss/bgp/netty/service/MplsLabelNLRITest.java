package com.jive.oss.bgp.netty.service;

import org.junit.Test;

public class MplsLabelNLRITest
{

  @Test
  public void test()
  {

    System.err.println(new MplsLabelNLRI(new byte[] { 0, 2, 1, 2, 3, 4, 5 }));

  }

}

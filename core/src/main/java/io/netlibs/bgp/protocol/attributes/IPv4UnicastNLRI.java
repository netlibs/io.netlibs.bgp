package io.netlibs.bgp.protocol.attributes;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.common.primitives.Longs;

import io.netlibs.bgp.protocol.NLRIHelper;
import io.netlibs.bgp.protocol.NetworkLayerReachabilityInformation;
import io.netlibs.ipaddr.CidrV4Address;
import io.netlibs.ipaddr.IPv4Address;
import lombok.Getter;

public class IPv4UnicastNLRI
{
  @Getter
  private NetworkLayerReachabilityInformation address;

  public IPv4UnicastNLRI(int pfxlen, byte[] data)
  {
    this.address = new NetworkLayerReachabilityInformation(pfxlen, NLRIHelper.trimNLRI(pfxlen, data));
  }

  public IPv4UnicastNLRI(byte[] data)
  {
    this.address = new NetworkLayerReachabilityInformation(data.length * 8, data);
  }

  public static IPv4UnicastNLRI fromCidrV4Address(CidrV4Address cidr)
  {
    // When we convert to an NLRI later, the bits we do not need get masked
    byte[] data = new byte[4];
    byte[] prefix = Longs.toByteArray(cidr.prefix());
    for (int i = 0; i < 4; i++)
    {
      data[i] = prefix[4 + i];
    }
    return new IPv4UnicastNLRI(cidr.mask(), data);
  }

  public static byte[] nlriTo32BIPv4(byte[] pfx)
  {
    // Pad to 32-bits such that it can be converted
    byte[] data = new byte[4];

    if (pfx.length != 4)
    {
      for (int i = 0; i < pfx.length; i++)
        data[i] = pfx[i];
      for (int i = pfx.length; i < 4; i++)
        data[i] = 0;
    }
    else
    {
      data = pfx;
    }
    return data;
  }

  public static byte[] longTo32BIPv4(long pfx)
  {
    byte[] addr = Longs.toByteArray(pfx);
    byte[] prefix = new byte[4];
    // convert the long address into a 32bit value to be handed to
    // the constructor
    for (int i = 0; i < 4; i++)
      prefix[i] = addr[4 + i];
    return prefix;
  }

  public CidrV4Address toCIDR()
  {
    byte[] data = nlriTo32BIPv4(this.address.getPrefix());

    return CidrV4Address.fromParts(IPv4Address.of(
        Longs.fromBytes(
            (byte)0, (byte)0, (byte)0, (byte)0, data[0], data[1], data[2], data[3])),
        address.getPrefixLength());
  }

  public InetAddress getInetAddress() throws UnknownHostException
  {
    byte[] data = nlriTo32BIPv4(this.address.getPrefix());
    return InetAddress.getByAddress(data);
  }

  public NetworkLayerReachabilityInformation getEncodedNlri()
  {
    return this.address;
  }

}
package io.netlibs.bgp.net.attributes;

import com.google.common.primitives.Ints;

import io.netlibs.bgp.net.NLRIHelper;
import io.netlibs.bgp.net.NetworkLayerReachabilityInformation;
import lombok.Getter;

public class AbstractMPLSLabelNLRI
{
  @Getter
  private int label;

  @Getter
  private final NetworkLayerReachabilityInformation address;

  @Getter
  private boolean bos;

  public AbstractMPLSLabelNLRI(final byte[] data)
  {
    // first 3 bytes are the label
    // last byte indicates TC and BOS bits. Currently indicated as 1.
    this.label = Ints.fromBytes(data[0], data[1], data[2], (byte) 0);
    this.label >>= 12;
    this.bos = (data[3] & 1) == 0;
    
    // find all but the first 3 bytes and convert them to the NLRI
    final byte[] pt = new byte[data.length - 3];
    System.arraycopy(data, 3, pt, 0, data.length - 3);

    this.address = new NetworkLayerReachabilityInformation((data.length - 3) * 8, pt);
  }
  
  public AbstractMPLSLabelNLRI(int label, boolean bos, int pfxlen, byte[] prefix){
    this.label = label;
    this.bos = bos;
    this.address = new NetworkLayerReachabilityInformation(pfxlen, NLRIHelper.trimNLRI(pfxlen, prefix));
  }
  
  public NetworkLayerReachabilityInformation getEncodedNLRI(){
    int finalLength = this.address.getPrefix().length + 3;
    byte[] finalNlri = new byte[finalLength];
    
    byte[] label = Ints.toByteArray(this.label << 12);
    byte[] encodedLabel = new byte[] { label[0], label[1], label[2] };
    
    // If the BOS bit is to be set then we need to set the last bit of the
    // 3-byte label value (BOS indicator). Hence OR it with 0x01.
    if(this.bos){
      encodedLabel[2] |= 1;
    }
    
    // Copy the encoded label into the first 3 bytes of the encoded NLRI
    for(int i=0; i<3;i++)
      finalNlri[i] = encodedLabel[i];
  
    // Copy the remaining length of the NLRI (prefix) into the encoded NLRI
    for(int i=0;i<this.address.getPrefix().length; i++)
      finalNlri[i+3] = this.address.getPrefix()[i];
    
    // Return an encoded NLRI value which can be used in generating an UPDATE message
    return new NetworkLayerReachabilityInformation(this.address.getPrefixLength()+(3*8), finalNlri);
  }
}
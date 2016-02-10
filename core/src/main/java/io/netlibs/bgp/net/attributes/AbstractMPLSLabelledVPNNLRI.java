package io.netlibs.bgp.net.attributes;

import java.net.UnknownHostException;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import io.netlibs.bgp.net.AbstractRouteDistinguisherType;
import io.netlibs.bgp.net.NetworkLayerReachabilityInformation;
import io.netlibs.bgp.net.RouteDistinguisherType0;
import io.netlibs.bgp.net.RouteDistinguisherType1;
import io.netlibs.bgp.net.RouteDistinguisherType2;
import io.netlibs.bgp.net.RouteDistinguisherUnknownType;
import io.netlibs.bgp.netty.service.NLRIHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class AbstractMPLSLabelledVPNNLRI
{
  @Getter
  private int label;
  
  @Getter
  private boolean bos;

  @Getter
  private AbstractRouteDistinguisherType rd;

  @Getter
  private NetworkLayerReachabilityInformation nlri;
  
  public AbstractMPLSLabelledVPNNLRI(byte[] data) throws UnknownHostException
  {
    // Format
    // Label: 3-bytes
    // RD:
    //    2-byte type
    //    6-byte value
    // Address:
    //    Remaining bytes
    
    this.label = Ints.fromBytes(data[0], data[1], data[2], (byte) 0);
    this.label >>= 12;    // bit-shift such that this is an int
    this.bos = (data[3] & 1) == 0;
    
    int rd_type = Ints.fromBytes((byte) 0, (byte) 0, data[3], data[4]);
    
    // Extract the RD contents
    byte[] rd_content = new byte[6];
    System.arraycopy(data, 5, rd_content, 0, 6);

    switch(rd_type)
    {
      case 0:
        this.rd = RouteDistinguisherType0.fromBytes(rd_content);
        break;
      case 1:
        this.rd = RouteDistinguisherType1.fromBytes(rd_content);
        break;
      case 2:
        this.rd = RouteDistinguisherType2.fromBytes(rd_content);
        break;
      default:
        this.rd = RouteDistinguisherUnknownType.fromBytes(rd_content);
        break;
    }
    
    // Remaining bytes are the IP prefix - minus the 3-byte label, and 8-byte
    // (RDType,RD) tuple.
    byte[] prefix = new byte[data.length - 3 - 8];
    System.arraycopy(data, 11, prefix, 0, data.length - 11);
    this.nlri = new NetworkLayerReachabilityInformation((data.length - 11) * 8, prefix);
  }
  
  public AbstractMPLSLabelledVPNNLRI(int label, AbstractRouteDistinguisherType rd, int prefixlen, byte[] prefix){
    this.label = label;
    this.rd = rd;
    this.nlri = new NetworkLayerReachabilityInformation(prefixlen, NLRIHelper.trimNLRI(prefixlen, prefix));
    // currently, we assume all service labels are BOS!
    this.bos = true;
  }
  
  public NetworkLayerReachabilityInformation getEncodedNLRI(){
    // prefixlength + 2 bytes RD type + 6 bytes RD + 3 bytes label
    int finalLength = this.nlri.getPrefix().length + 2 + 6 + 3;
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
    
    // Copy the RD type into the buffer
    for(int i=0; i<2; i++)
      finalNlri[i+3] = rd.getType()[i];
 
    // Encode the 6-byte RD
    byte[] rd_content = this.rd.getBytes();
    for(int i=0; i<6; i++)
      finalNlri[i+5] = rd_content[i];
  
    // Copy the remaining length of the NLRI (prefix) into the encoded NLRI
    for(int i=0;i<this.nlri.getPrefix().length; i++)
      finalNlri[i+11] = this.nlri.getPrefix()[i];
    
    // Return an encoded NLRI value which can be used in generating an UPDATE message
    return new NetworkLayerReachabilityInformation(this.nlri.getPrefixLength()+((3+2+6)*8), finalNlri);
  }
}

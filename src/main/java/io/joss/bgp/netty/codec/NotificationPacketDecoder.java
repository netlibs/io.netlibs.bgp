package io.joss.bgp.netty.codec;

import io.joss.bgp.net.AddressFamily;
import io.joss.bgp.net.SubsequentAddressFamily;
import io.joss.bgp.netty.BGPv4Constants;
import io.joss.bgp.netty.protocol.AdministrativeResetNotificationPacket;
import io.joss.bgp.netty.protocol.AdministrativeShutdownNotificationPacket;
import io.joss.bgp.netty.protocol.BGPv4Packet;
import io.joss.bgp.netty.protocol.BadMessageLengthNotificationPacket;
import io.joss.bgp.netty.protocol.BadMessageTypeNotificationPacket;
import io.joss.bgp.netty.protocol.CeaseNotificationPacket;
import io.joss.bgp.netty.protocol.ConnectionCollisionResolutionNotificationPacket;
import io.joss.bgp.netty.protocol.ConnectionNotSynchronizedNotificationPacket;
import io.joss.bgp.netty.protocol.ConnectionRejectedNotificationPacket;
import io.joss.bgp.netty.protocol.FiniteStateMachineErrorNotificationPacket;
import io.joss.bgp.netty.protocol.HoldTimerExpiredNotificationPacket;
import io.joss.bgp.netty.protocol.MaximumNumberOfPrefixesReachedNotificationPacket;
import io.joss.bgp.netty.protocol.MessageHeaderErrorNotificationPacket;
import io.joss.bgp.netty.protocol.NotificationPacket;
import io.joss.bgp.netty.protocol.OtherConfigurationChangeNotificationPacket;
import io.joss.bgp.netty.protocol.OutOfResourcesNotificationPacket;
import io.joss.bgp.netty.protocol.PeerDeconfiguredNotificationPacket;
import io.joss.bgp.netty.protocol.UnspecifiedCeaseNotificationPacket;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificationPacketDecoder
{

  /**
   * decode the NOTIFICATION network packet. The NOTIFICATION packet must be at least 2 octets large at this point.
   *
   * @param buffer
   *          the buffer containing the data.
   * @return
   */

  public BGPv4Packet decodeNotificationPacket(final ByteBuf buffer)
  {

    NotificationPacket packet = null;

    ProtocolPacketUtils.verifyPacketSize(buffer, BGPv4Constants.BGP_PACKET_MIN_SIZE_NOTIFICATION, -1);

    final int errorCode = buffer.readUnsignedByte();
    final int errorSubcode = buffer.readUnsignedByte();

    switch (errorCode)
    {

      case BGPv4Constants.BGP_ERROR_CODE_MESSAGE_HEADER:
        packet = this.decodeMessageHeaderNotificationPacket(buffer, errorSubcode);
        break;

      case BGPv4Constants.BGP_ERROR_CODE_OPEN:
        packet = BGPv4PacketDecoder.openPacketDecoder.decodeOpenNotificationPacket(buffer, errorSubcode);
        break;

      case BGPv4Constants.BGP_ERROR_CODE_UPDATE:
        packet = BGPv4PacketDecoder.updatePacketDecoder.decodeUpdateNotification(buffer, errorSubcode);
        break;

      case BGPv4Constants.BGP_ERROR_CODE_HOLD_TIMER_EXPIRED:
        packet = new HoldTimerExpiredNotificationPacket();
        break;

      case BGPv4Constants.BGP_ERROR_CODE_FINITE_STATE_MACHINE_ERROR:
        packet = new FiniteStateMachineErrorNotificationPacket();
        break;

      case BGPv4Constants.BGP_ERROR_CODE_CEASE:
        packet = this.decodeCeaseNotificationPacket(buffer, errorSubcode);
        break;

    }

    return packet;
  }

  /**
   * decode the NOTIFICATION network packet for error code "Message Header Error".
   *
   * @param buffer
   *          the buffer containing the data.
   * @return
   */
  private NotificationPacket decodeMessageHeaderNotificationPacket(final ByteBuf buffer, final int errorSubcode)
  {
    NotificationPacket packet = null;

    switch (errorSubcode)
    {
      case MessageHeaderErrorNotificationPacket.SUBCODE_CONNECTION_NOT_SYNCHRONIZED:
        packet = new ConnectionNotSynchronizedNotificationPacket();
        break;
      case MessageHeaderErrorNotificationPacket.SUBCODE_BAD_MESSAGE_LENGTH:
        packet = new BadMessageLengthNotificationPacket(buffer.readUnsignedShort());
        break;
      case MessageHeaderErrorNotificationPacket.SUBCODE_BAD_MESSAGE_TYPE:
        packet = new BadMessageTypeNotificationPacket(buffer.readUnsignedByte());
        break;
    }

    return packet;
  }

  /**
   * decode the NOTIFICATION network packet for error code "Cease".
   *
   * @param buffer
   *          the buffer containing the data.
   * @return
   */
  private NotificationPacket decodeCeaseNotificationPacket(final ByteBuf buffer, final int errorSubcode)
  {
    NotificationPacket packet = null;

    switch (errorSubcode)
    {
      default:
        // this.log.info("cannot handle cease notification subcode {}", errorSubcode);
      case CeaseNotificationPacket.SUBCODE_UNSPECIFIC:
        packet = new UnspecifiedCeaseNotificationPacket();
        break;
      case CeaseNotificationPacket.SUBCODE_MAXIMUM_NUMBER_OF_PREFIXES_REACHED:
        packet = new MaximumNumberOfPrefixesReachedNotificationPacket();

        try
        {
          final AddressFamily afi = AddressFamily.fromCode(buffer.readUnsignedShort());
          final SubsequentAddressFamily safi = SubsequentAddressFamily.fromCode(buffer.readUnsignedByte());
          final int prefixUpperBounds = (int) buffer.readUnsignedInt();
          packet = new MaximumNumberOfPrefixesReachedNotificationPacket(afi, safi, prefixUpperBounds);
        }
        catch (final RuntimeException e)
        {
          log.error("Error parsing", e);
          throw e;
        }
        break;
      case CeaseNotificationPacket.SUBCODE_ADMINSTRATIVE_SHUTDOWN:
        packet = new AdministrativeShutdownNotificationPacket();
        break;
      case CeaseNotificationPacket.SUBCODE_PEER_DECONFIGURED:
        packet = new PeerDeconfiguredNotificationPacket();
        break;
      case CeaseNotificationPacket.SUBCODE_ADMINSTRATIVE_RESET:
        packet = new AdministrativeResetNotificationPacket();
        break;
      case CeaseNotificationPacket.SUBCODE_CONNECTION_REJECTED:
        packet = new ConnectionRejectedNotificationPacket();
        break;
      case CeaseNotificationPacket.SUBCODE_OTHER_CONFIGURATION_CHANGE:
        packet = new OtherConfigurationChangeNotificationPacket();
        break;
      case CeaseNotificationPacket.SUBCODE_CONNECTION_COLLISION_RESOLUTION:
        packet = new ConnectionCollisionResolutionNotificationPacket();
        break;
      case CeaseNotificationPacket.SUBCODE_OUT_OF_RESOURCES:
        packet = new OutOfResourcesNotificationPacket();
        break;
    }

    return packet;
  }

}

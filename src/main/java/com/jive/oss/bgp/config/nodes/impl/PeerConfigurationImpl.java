/**
 *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * File: org.bgp4.config.nodes.impl.PeerConfigurationImpl.java
 */
package com.jive.oss.bgp.config.nodes.impl;

import javax.naming.ConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.jive.oss.bgp.config.nodes.Capabilities;
import com.jive.oss.bgp.config.nodes.ClientConfiguration;
import com.jive.oss.bgp.config.nodes.PeerConfiguration;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class PeerConfigurationImpl implements PeerConfiguration
{

  private ClientConfiguration clientConfig;
  private int localAS;
  private int remoteAS;
  private String peerName;
  private long localBgpIdentifier;
  private long remoteBgpIdentifier;
  private int holdTime = 30;
  private boolean holdTimerDisabled;
  private int idleHoldTime = 30;
  private boolean allowAutomaticStart;
  private boolean allowAutomaticStop;
  private boolean collisionDetectEstablishedState;
  private boolean dampPeerOscillation;
  private boolean delayOpen;
  private boolean passiveTcpEstablishment;
  private int delayOpenTime = 5;
  private int connectRetryTime = 5;
  private int automaticStartInterval = 5;
  private Capabilities capabilities = new CapabilitiesImpl();

  public PeerConfigurationImpl()
  {
  }

  public PeerConfigurationImpl(final String peerName, final ClientConfiguration clientConfig) throws ConfigurationException
  {
    this.setPeerName(peerName);
    this.setClientConfig(clientConfig);
  }

  public PeerConfigurationImpl(final String peerName, final ClientConfiguration clientConfig, final int localAS, final int remoteAS) throws ConfigurationException
  {
    this(peerName, clientConfig);
    this.setLocalAS(localAS);
    this.setRemoteAS(remoteAS);
  }

  public PeerConfigurationImpl(final String peerName, final ClientConfiguration clientConfig, final int localAS, final int remoteAS,
      final long localBgpIdentifier, final long remoteBgpIdentifier) throws ConfigurationException
  {
    this(peerName, clientConfig, localAS, remoteAS);

    this.setLocalBgpIdentifier(localBgpIdentifier);
    this.setRemoteBgpIdentifier(remoteBgpIdentifier);
  }

  public PeerConfigurationImpl(final String peerName, final ClientConfiguration clientConfig, final int localAS, final int remoteAS,
      final long localBgpIdentifier, final long remoteBgpIdentifier, final int connectRetryTime) throws ConfigurationException
  {
    this(peerName, clientConfig, localAS, remoteAS, localBgpIdentifier, remoteBgpIdentifier);

    this.connectRetryTime = connectRetryTime;
  }

  public PeerConfigurationImpl(final String peerName, final ClientConfiguration clientConfig, final int localAS, final int remoteAS,
      final long localBgpIdentifier, final long remoteBgpIdentifier, final int connectRetryTime, final int holdTime, final boolean holdTimerDisabled, final int idleHoldTime)
          throws ConfigurationException
  {
    this(peerName, clientConfig, localAS, remoteAS, localBgpIdentifier, remoteBgpIdentifier, connectRetryTime);

    this.setHoldTime(holdTime);
    this.setHoldTimerDisabled(holdTimerDisabled);
    this.setIdleHoldTime(idleHoldTime);
  }

  public PeerConfigurationImpl(final String peerName, final ClientConfiguration clientConfig, final int localAS, final int remoteAS,
      final long localBgpIdentifier, final long remoteBgpIdentifier, final int connectRetryTime, final int holdTime, final boolean holdTimerDisabled, final int idleHoldTime,
      final boolean allowAutomaticStart, final boolean allowAutomaticStop, final int automaticStartInterval, final boolean dampPeerOscillation) throws ConfigurationException
  {
    this(peerName, clientConfig, localAS, remoteAS, localBgpIdentifier, remoteBgpIdentifier, connectRetryTime, holdTime, holdTimerDisabled, idleHoldTime);

    this.setAllowAutomaticStart(allowAutomaticStart);
    this.setAllowAutomaticStop(allowAutomaticStop);
    this.setAutomaticStartInterval(automaticStartInterval);
    this.setDampPeerOscillation(dampPeerOscillation);
  }

  public PeerConfigurationImpl(final String peerName, final ClientConfiguration clientConfig, final int localAS, final int remoteAS,
      final long localBgpIdentifier, final long remoteBgpIdentifier, final int connectRetryTime, final int holdTime, final boolean holdTimerDisabled, final int idleHoldTime,
      final boolean allowAutomaticStart, final boolean allowAutomaticStop, final int automaticStartInterval, final boolean dampPeerOscillation,
      final boolean passiveTcpEstablishment, final boolean delayOpen, final int delayOpenTime) throws ConfigurationException
  {
    this(peerName, clientConfig, localAS, remoteAS, localBgpIdentifier, remoteBgpIdentifier, connectRetryTime, holdTime, holdTimerDisabled, idleHoldTime,
        allowAutomaticStart, allowAutomaticStop, automaticStartInterval, dampPeerOscillation);

    this.setPassiveTcpEstablishment(passiveTcpEstablishment);
    this.setDelayOpen(delayOpen);
    this.setDelayOpenTime(delayOpenTime);
  }

  public PeerConfigurationImpl(final String peerName, final ClientConfiguration clientConfig, final int localAS, final int remoteAS,
      final long localBgpIdentifier, final long remoteBgpIdentifier, final int connectRetryTime, final int holdTime, final boolean holdTimerDisabled, final int idleHoldTime,
      final boolean allowAutomaticStart, final boolean allowAutomaticStop, final int automaticStartInterval, final boolean dampPeerOscillation,
      final boolean passiveTcpEstablishment, final boolean delayOpen, final int delayOpenTime,
      final boolean collisionDetectEstablishedState) throws ConfigurationException
  {
    this(peerName, clientConfig, localAS, remoteAS, localBgpIdentifier, remoteBgpIdentifier, connectRetryTime, holdTime, holdTimerDisabled, idleHoldTime,
        allowAutomaticStart, allowAutomaticStop, automaticStartInterval, dampPeerOscillation, passiveTcpEstablishment, delayOpen, delayOpenTime);

    this.setCollisionDetectEstablishedState(collisionDetectEstablishedState);
  }

  public PeerConfigurationImpl(final String peerName, final ClientConfiguration clientConfig, final int localAS, final int remoteAS,
      final long localBgpIdentifier, final long remoteBgpIdentifier, final int connectRetryTime, final int holdTime, final boolean holdTimerDisabled, final int idleHoldTime,
      final boolean allowAutomaticStart, final boolean allowAutomaticStop, final int automaticStartInterval, final boolean dampPeerOscillation,
      final boolean passiveTcpEstablishment, final boolean delayOpen, final int delayOpenTime,
      final boolean collisionDetectEstablishedState, final Capabilities capabilities) throws ConfigurationException
  {
    this(peerName, clientConfig, localAS, remoteAS, localBgpIdentifier, remoteBgpIdentifier, connectRetryTime, holdTime, holdTimerDisabled, idleHoldTime,
        allowAutomaticStart, allowAutomaticStop, automaticStartInterval, dampPeerOscillation, passiveTcpEstablishment, delayOpen, delayOpenTime);

    this.setCollisionDetectEstablishedState(collisionDetectEstablishedState);

    if (capabilities != null)
    {
      this.setCapabilities(capabilities);
    }
  }

  @Override
  public ClientConfiguration getClientConfig()
  {
    return this.clientConfig;
  }

  @Override
  public int getLocalAS()
  {
    return this.localAS;
  }

  @Override
  public int getRemoteAS()
  {
    return this.remoteAS;
  }

  /**
   * @param clientConfig
   *          the clientConfig to set
   * @throws ConfigurationException
   */
  void setClientConfig(ClientConfiguration clientConfig) throws ConfigurationException
  {
    if (clientConfig == null)
    {
      throw new ConfigurationException("null client configuration not allowed");
    }

    if (!(clientConfig instanceof BgpClientPortConfigurationDecorator))
    {
      clientConfig = new BgpClientPortConfigurationDecorator(clientConfig);
    }

    this.clientConfig = clientConfig;
  }

  /**
   * @param localAS
   *          the localAS to set
   */
  void setLocalAS(final int localAS) throws ConfigurationException
  {
    if (localAS <= 0)
    {
      throw new ConfigurationException("negative AS number not allowed");
    }

    this.localAS = localAS;
  }

  /**
   * @param remoteAS
   *          the remoteAS to set
   * @throws ConfigurationException
   */
  void setRemoteAS(final int remoteAS) throws ConfigurationException
  {
    if (remoteAS <= 0)
    {
      throw new ConfigurationException("negative AS number not allowed");
    }

    this.remoteAS = remoteAS;
  }

  /**
   * @return the name
   */
  @Override
  public String getPeerName()
  {
    return this.peerName;
  }

  /**
   * @param name
   *          the name to set
   * @throws ConfigurationException
   */
  void setPeerName(final String name) throws ConfigurationException
  {
    if (StringUtils.isBlank(name))
    {
      throw new ConfigurationException("blank name not allowed");
    }

    this.peerName = name;
  }

  /**
   * @return the localBgpIdentifier
   */
  @Override
  public long getLocalBgpIdentifier()
  {
    return this.localBgpIdentifier;
  }

  /**
   * @param localBgpIdentifier
   *          the localBgpIdentifier to set
   */
  void setLocalBgpIdentifier(final long localBgpIdentifier) throws ConfigurationException
  {
    if (localBgpIdentifier <= 0)
    {
      throw new ConfigurationException("Illegal local BGP identifier: " + localBgpIdentifier);
    }
    this.localBgpIdentifier = localBgpIdentifier;
  }

  /**
   * @return the remoteBgpIdentifier
   */
  @Override
  public long getRemoteBgpIdentifier()
  {
    return this.remoteBgpIdentifier;
  }

  /**
   * @param remoteBgpIdentifier
   *          the remoteBgpIdentifier to set
   */
  void setRemoteBgpIdentifier(final long remoteBgpIdentifier) throws ConfigurationException
  {
    if (remoteBgpIdentifier <= 0)
    {
      throw new ConfigurationException("Illegal remote BGP identifier: " + remoteBgpIdentifier);
    }
    this.remoteBgpIdentifier = remoteBgpIdentifier;
  }

  /**
   * @return the holdTime
   */
  @Override
  public int getHoldTime()
  {
    return this.holdTime;
  }

  /**
   * @param holdTime
   *          the holdTime to set
   */
  void setHoldTime(final int holdTime) throws ConfigurationException
  {
    if (holdTime < 0)
    {
      throw new ConfigurationException("Illegal hold time given: " + holdTime);
    }

    this.holdTime = holdTime;
  }

  /**
   * @return the connectRetryInterval
   */
  @Override
  public int getIdleHoldTime()
  {
    return this.idleHoldTime;
  }

  /**
   * @param connectRetryInterval
   *          the connectRetryInterval to set
   */
  void setIdleHoldTime(final int connectRetryInterval) throws ConfigurationException
  {
    if (connectRetryInterval < 0)
    {
      throw new ConfigurationException("Illegal connect retry interval given: " + connectRetryInterval);
    }

    this.idleHoldTime = connectRetryInterval;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return (new HashCodeBuilder())
        .append(this.allowAutomaticStart)
        .append(this.allowAutomaticStop)
        .append(this.automaticStartInterval)
        .append(this.capabilities)
        .append(this.clientConfig)
        .append(this.collisionDetectEstablishedState)
        .append(this.connectRetryTime)
        .append(this.dampPeerOscillation)
        .append(this.delayOpen)
        .append(this.delayOpenTime)
        .append(this.holdTime)
        .append(this.holdTimerDisabled)
        .append(this.idleHoldTime)
        .append(this.localAS)
        .append(this.localBgpIdentifier)
        .append(this.passiveTcpEstablishment)
        .append(this.peerName)
        .append(this.remoteAS)
        .append(this.remoteBgpIdentifier)
        .toHashCode();

  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    /*
     * if(getClass() != obj.getClass()) return false;
     *
     * PeerConfigurationImpl o = (PeerConfigurationImpl)obj;
     *
     * return (new EqualsBuilder()) .append(allowAutomaticStart, o.allowAutomaticStart) .append(allowAutomaticStop, o.allowAutomaticStop)
     * .append(automaticStartInterval, o.automaticStartInterval) .append(capabilities, o.capabilities) .append(clientConfig, o.clientConfig)
     * .append(collisionDetectEstablishedState, o.collisionDetectEstablishedState) .append(connectRetryTime, o.connectRetryTime)
     * .append(dampPeerOscillation, o.dampPeerOscillation) .append(delayOpen, o.delayOpen) .append(delayOpenTime, o.delayOpenTime)
     * .append(holdTime, o.holdTime) .append(holdTimerDisabled, o.holdTimerDisabled) .append(idleHoldTime, o.idleHoldTime) .append(localAS,
     * o.localAS) .append(localBgpIdentifier, o.localBgpIdentifier) .append(passiveTcpEstablishment, o.passiveTcpEstablishment)
     * .append(peerName, o.peerName) .append(remoteAS, o.remoteAS) .append(remoteBgpIdentifier, o.remoteBgpIdentifier) .isEquals();
     */
    if (!PeerConfiguration.class.isAssignableFrom(obj.getClass()))
    {
      return false;
    }

    final PeerConfiguration o = (PeerConfiguration) obj;

    return (new EqualsBuilder())
        .append(this.allowAutomaticStart, o.isAllowAutomaticStart())
        .append(this.allowAutomaticStop, o.isAllowAutomaticStop())
        .append(this.automaticStartInterval, o.getAutomaticStartInterval())
        .append(this.capabilities, o.getCapabilities())
        .append(this.clientConfig, o.getClientConfig())
        .append(this.collisionDetectEstablishedState, o.isCollisionDetectEstablishedState())
        .append(this.connectRetryTime, o.getConnectRetryTime())
        .append(this.dampPeerOscillation, o.isDampPeerOscillation())
        .append(this.delayOpen, o.isDelayOpen())
        .append(this.delayOpenTime, o.getDelayOpenTime())
        .append(this.holdTime, o.getHoldTime())
        .append(this.holdTimerDisabled, o.isHoldTimerDisabled())
        .append(this.idleHoldTime, o.getIdleHoldTime())
        .append(this.localAS, o.getLocalAS())
        .append(this.localBgpIdentifier, o.getLocalBgpIdentifier())
        .append(this.passiveTcpEstablishment, o.isPassiveTcpEstablishment())
        .append(this.peerName, o.getPeerName())
        .append(this.remoteAS, o.getRemoteAS())
        .append(this.remoteBgpIdentifier, o.getRemoteBgpIdentifier())
        .isEquals();
  }

  /**
   * @return the allowAutomaticStart
   */
  @Override
  public boolean isAllowAutomaticStart()
  {
    return this.allowAutomaticStart;
  }

  /**
   * @param allowAutomaticStart
   *          the allowAutomaticStart to set
   */
  void setAllowAutomaticStart(final boolean allowAutomaticStart)
  {
    this.allowAutomaticStart = allowAutomaticStart;
  }

  /**
   * @return the allowAutomaticStop
   */
  @Override
  public boolean isAllowAutomaticStop()
  {
    return this.allowAutomaticStop;
  }

  /**
   * @param allowAutomaticStop
   *          the allowAutomaticStop to set
   */
  void setAllowAutomaticStop(final boolean allowAutomaticStop)
  {
    this.allowAutomaticStop = allowAutomaticStop;
  }

  /**
   * @return the collisionDetectEstablishedEnabledState
   */
  @Override
  public boolean isCollisionDetectEstablishedState()
  {
    return this.collisionDetectEstablishedState;
  }

  /**
   * @param collisionDetectEstablishedEnabledState
   *          the collisionDetectEstablishedEnabledState to set
   */
  void setCollisionDetectEstablishedState(
      final boolean collisionDetectEstablishedEnabledState)
  {
    this.collisionDetectEstablishedState = collisionDetectEstablishedEnabledState;
  }

  /**
   * @return the dampPeerOscillation
   */
  @Override
  public boolean isDampPeerOscillation()
  {
    return this.dampPeerOscillation;
  }

  /**
   * @param dampPeerOscillation
   *          the dampPeerOscillation to set
   */
  void setDampPeerOscillation(final boolean dampPeerOscillation)
  {
    this.dampPeerOscillation = dampPeerOscillation;
  }

  /**
   * @return the delayOpen
   */
  @Override
  public boolean isDelayOpen()
  {
    return this.delayOpen;
  }

  /**
   * @param delayOpen
   *          the delayOpen to set
   */
  void setDelayOpen(final boolean delayOpen)
  {
    this.delayOpen = delayOpen;
  }

  /**
   * @return the passiveTcpEstablishment
   */
  @Override
  public boolean isPassiveTcpEstablishment()
  {
    return this.passiveTcpEstablishment;
  }

  /**
   * @param passiveTcpEstablishment
   *          the passiveTcpEstablishment to set
   */
  void setPassiveTcpEstablishment(final boolean passiveTcpEstablishment)
  {
    this.passiveTcpEstablishment = passiveTcpEstablishment;
  }

  /**
   * @return the delayOpenTime
   */
  @Override
  public int getDelayOpenTime()
  {
    return this.delayOpenTime;
  }

  /**
   * @param delayOpenTime
   *          the delayOpenTime to set
   * @throws ConfigurationException
   */
  void setDelayOpenTime(final int delayOpenTime) throws ConfigurationException
  {
    if (delayOpenTime < 0)
    {
      throw new ConfigurationException("Illegal delay open time given: " + delayOpenTime);
    }

    this.delayOpenTime = delayOpenTime;
  }

  /**
   * @return the connectRetryTime
   */
  @Override
  public int getConnectRetryTime()
  {
    return this.connectRetryTime;
  }

  /**
   * @param connectRetryTime
   *          the connectRetryTime to set
   */
  void setConnectRetryTime(final int connectRetryTime) throws ConfigurationException
  {
    if (connectRetryTime < 0)
    {
      throw new ConfigurationException("Illegal connect retry time given: " + connectRetryTime);
    }

    this.connectRetryTime = connectRetryTime;
  }

  /**
   * @return the automaticStartInterval
   */
  @Override
  public int getAutomaticStartInterval()
  {
    return this.automaticStartInterval;
  }

  /**
   * @param automaticStartInterval
   *          the automaticStartInterval to set
   */
  void setAutomaticStartInterval(final int automaticStartInterval) throws ConfigurationException
  {
    if (automaticStartInterval < 0)
    {
      throw new ConfigurationException("Illegal automatic start interval given: " + this.connectRetryTime);
    }

    this.automaticStartInterval = automaticStartInterval;
  }

  /**
   * @return the holdTimerDisabled
   */
  @Override
  public boolean isHoldTimerDisabled()
  {
    return this.holdTimerDisabled;
  }

  /**
   * @param holdTimerDisabled
   *          the holdTimerDisabled to set
   */
  void setHoldTimerDisabled(final boolean holdTimerDisabled)
  {
    this.holdTimerDisabled = holdTimerDisabled;
  }

  /**
   * @return the capabilities
   */
  @Override
  public Capabilities getCapabilities()
  {
    return this.capabilities;
  }

  /**
   * @param capabilities
   *          the capabilities to set
   */
  public void setCapabilities(final Capabilities capabilities)
  {
    this.capabilities = capabilities;
  }

}

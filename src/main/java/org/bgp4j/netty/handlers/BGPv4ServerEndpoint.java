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
 */
package org.bgp4j.netty.handlers;

import java.net.InetSocketAddress;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bgp4j.netty.PeerConnectionInformation;
import org.bgp4j.netty.PeerConnectionInformationAware;
import org.bgp4j.netty.fsm.BGPv4FSM;
import org.bgp4j.netty.fsm.FSMRegistry;
import org.bgp4j.netty.protocol.BGPv4Packet;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.slf4j.Logger;

/**
 * This handler acts as the client side pipeline end. It attaches the peer connection info to the channel context of all insterested 
 * handlers when the channel is connected. Each message it receives is forwarded to the appropiate finite state machine instance.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
@Singleton
public class BGPv4ServerEndpoint extends SimpleChannelHandler {
	public static final String HANDLER_NAME ="BGP4-ServerEndpoint";

	private @Inject Logger log;
	private @Inject FSMRegistry fsmRegistry;
	private ChannelGroup trackedChannels = new DefaultChannelGroup(HANDLER_NAME);
	
	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.MessageEvent)
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		BGPv4FSM fsm = fsmRegistry.lookupFSM(((InetSocketAddress)e.getRemoteAddress()).getAddress());
			
		if(fsm == null) {
			log.error("Internal Error: client for address " + e.getRemoteAddress() + " is unknown");
			
			ctx.getChannel().close();
		} else {
			if(e.getMessage() instanceof BGPv4Packet) {
				fsm.handleMessage(ctx.getChannel(), (BGPv4Packet)e.getMessage());
			} else {
				log.error("unknown payload class " + e.getMessage().getClass().getName() + " received for peer " + e.getRemoteAddress());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		Channel clientChannel = e.getChannel();
		log.info("connected to client " + clientChannel.getRemoteAddress());
		
		BGPv4FSM fsm = fsmRegistry.lookupFSM(((InetSocketAddress)clientChannel.getRemoteAddress()).getAddress());
		
		if(fsm == null) {
			log.error("Internal Error: client for address " + clientChannel.getRemoteAddress() + " is unknown");
			
			clientChannel.close();
			clientChannel = null;
		} else if(fsm.isCanAcceptConnection()) {
			ChannelPipeline pipeline = ctx.getPipeline();
			PeerConnectionInformation pci = fsm.getPeerConnectionInformation();

			for (String handlerName : pipeline.getNames()) {
				ChannelHandler handler = pipeline.get(handlerName);

				if (handler.getClass().isAnnotationPresent(PeerConnectionInformationAware.class)) {
					log.info("attaching peer connection information " + pci
							+ " to handler " + handlerName + " for client "
							+ clientChannel.getRemoteAddress());

					pipeline.getContext(handlerName).setAttachment(pci);
				}
			}

			fsm.handleServerOpened(clientChannel);
		} else {
			log.info("Connection from client " + e.getChannel().getRemoteAddress() + " cannot be accepted");
			
			clientChannel.close();
			clientChannel = null;
		}
		
		if(clientChannel != null) {
			trackedChannels.add(clientChannel);
			ctx.sendUpstream(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
	 */
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.info("closed connection to client " + e.getChannel().getRemoteAddress());
		
		BGPv4FSM fsm = fsmRegistry.lookupFSM(((InetSocketAddress)e.getChannel().getRemoteAddress()).getAddress());
		
		if(fsm == null) {
			log.error("Internal Error: client for address " + e.getChannel().getRemoteAddress() + " is unknown");
			
			ctx.getChannel().close();
		} else {
			
			fsm.handleClosed(e.getChannel());
			ctx.sendUpstream(e);
		}
		
		trackedChannels.remove(e.getChannel());
	}

	/**
	 * @return the trackedChannels
	 */
	public ChannelGroup getTrackedChannels() {
		return trackedChannels;
	}
}

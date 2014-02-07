package com.alecgorge.minecraft.jsonapi.packets;

import net.minecraft.server.v1_7_R1.EnumProtocol;

import org.bukkit.plugin.Plugin;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.utility.MinecraftVersion;

public class ProtocolLibHTTPInjector implements PacketListener {
	ProtocolManager lib = ProtocolLibrary.getProtocolManager();
	JSONAPI api;
	
	@SuppressWarnings("unchecked")
	public ProtocolLibHTTPInjector(JSONAPI api) {
		this.api = api;
		
		try {
			EnumProtocol.a(0).a().put(0x47, Packet0x47HttpGetPacket.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void inject() {
		lib.addPacketListener(new PacketAdapter(api, ConnectionSide.BOTH, GamePhase.BOTH, 0x47) {
			@Override
			public void onPacketReceiving(PacketEvent e) {
				JSONAPI.dbug(e.getPacketID());
				JSONAPI.dbug(e.getPacketType());
				JSONAPI.dbug(e.getPacket().getStrings());
			}
		});
	}

	@Override
	public Plugin getPlugin() {
		return api;
	}

	@Override
	public ListeningWhitelist getReceivingWhitelist() {
		ListeningWhitelist w = ListeningWhitelist.newBuilder().
													highest().
													gamePhaseBoth().
													options(new ListenerOptions[] { ListenerOptions.ASYNC, ListenerOptions.DISABLE_GAMEPHASE_DETECTION, ListenerOptions.INTERCEPT_INPUT_BUFFER }).
													types(new PacketType(PacketType.Protocol.HANDSHAKING, PacketType.Sender.CLIENT, 0x47, -1)).
													build();
		return w;
	}

	@Override
	public ListeningWhitelist getSendingWhitelist() {
		return ListeningWhitelist.newBuilder().monitor().build();
	}

	@Override
	public void onPacketReceiving(PacketEvent e) {
		JSONAPI.dbug(e.getPacketID());
		JSONAPI.dbug(e.getPacketType());
		JSONAPI.dbug(e.getPacket().getStrings());
	}

	@Override
	public void onPacketSending(PacketEvent e) {
			
	}
}

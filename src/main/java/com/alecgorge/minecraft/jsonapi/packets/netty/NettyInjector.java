package com.alecgorge.minecraft.jsonapi.packets.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Lists;

public abstract class NettyInjector {   
    // The temporary player factory
    private List<VolatileField> bootstrapFields = Lists.newArrayList();
   
    // List of network managers
    private volatile List<Object> networkManagers;
    private boolean injected;

    private boolean closed;

    /**
     * Inject into the spigot connection class.
     */
    @SuppressWarnings("unchecked")
    public synchronized void inject() {
        if (injected)
            throw new IllegalStateException("Cannot inject twice.");
        try {
            FuzzyReflection fuzzyServer = FuzzyReflection.fromClass(MinecraftReflection.getMinecraftServerClass());
            Method serverConnectionMethod = fuzzyServer.getMethodByParameters("getServerConnection", MinecraftReflection.getServerConnectionClass(), new Class[] {});
            
            // Get the server connection
            Object server = fuzzyServer.getSingleton();
            Object serverConnection = serverConnectionMethod.invoke(server);
            
            // Handle connected channels
            final ChannelInboundHandler endInitProtocol = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    try {
                        // This can take a while, so we need to stop the main thread from interfering
                        synchronized (networkManagers) {
                            injectChannel(channel);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            
            // This is executed before Minecraft's channel handler
            final ChannelInboundHandler beginInitProtocol = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    // Our only job is to add init protocol
                    channel.pipeline().addLast(endInitProtocol);
                }
            };
            
            // Add our handler to newly created channels
            final ChannelHandler connectionHandler = new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    Channel channel = (Channel) msg;

                    // Prepare to initialize ths channel
                    channel.pipeline().addFirst(beginInitProtocol);
                    ctx.fireChannelRead(msg);
                }
            };
            
            // Get the current NetworkMananger list
            networkManagers = (List<Object>) FuzzyReflection.fromObject(serverConnection, true).
                invokeMethod(null, "getNetworkManagers", List.class, serverConnection);
            
            // Insert ProtocolLib's connection interceptor
            bootstrapFields = getBootstrapFields(serverConnection);
            
            for (VolatileField field : bootstrapFields) {
                final List<Object> list = (List<Object>) field.getValue();
     
                // We don't have to override this list
                if (list == networkManagers) {
                    continue;
                }
                
                // Synchronize with each list before we attempt to replace them.
                field.setValue(new BootstrapList(list, connectionHandler));
            }

            injected = true;
            
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject channel futures.", e);
        }
    }
    
    /**
     * Invoked when a channel is ready to be injected.
     * @param channel - the channel to inject.
     */
    protected abstract void injectChannel(Channel channel);
    
    /**
     * Retrieve a list of every field with a list of channel futures.
     * @param serverConnection - the connection.
     * @return List of fields.
     */
    private List<VolatileField> getBootstrapFields(Object serverConnection) {
        List<VolatileField> result = Lists.newArrayList();
        
        // Find and (possibly) proxy every list
        for (Field field : FuzzyReflection.fromObject(serverConnection, true).getFieldListByType(List.class)) {
            VolatileField volatileField = new VolatileField(field, serverConnection, true).toSynchronized();
            
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) volatileField.getValue();
            
            if (list.size() == 0 || list.get(0) instanceof ChannelFuture) {
                result.add(volatileField);
            }
        }
        return result;
    }
    
    /**
     * Clean up any remaning injections.
     */
    public synchronized void close() {
        if (!closed) {
            closed = true;

            for (VolatileField field : bootstrapFields) {
                Object value = field.getValue();

                // Undo the processed channels, if any 
                if (value instanceof BootstrapList) {
                    ((BootstrapList) value).close();
                }
                field.revertValue();
            }
        }
    }
}

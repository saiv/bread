package it.saiv.bread.net;

import it.saiv.bread.event.Loop;
import it.saiv.bread.streaming.Stream;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.security.cert.X509Certificate;

public class Connection extends Stream<SelectableChannel> {
    protected SocketChannel channel;

    public Connection(SocketChannel channel, Loop<SelectableChannel> loop) {
        super(channel, loop);
    }

    public InetAddress getRemoteAddress() throws IOException {
        InetSocketAddress isa = (InetSocketAddress) this.channel.getRemoteAddress();
        return isa.getAddress();
    }

    public int getRemotePort() throws IOException {
        InetSocketAddress isa = (InetSocketAddress) this.channel.getRemoteAddress();
        return isa.getPort();
    }

    public boolean isSecure() {
        return false;
    }

    public boolean isClientIdentified() {
        return false;
    }

    public X509Certificate getClientIdentity() {
        // TODO throw ClientNotIdentifiedException
        return null;
    }

    public X509Certificate getServerIdentity() {
        // TODO throw ServerNotIdentifiedException
        return null;
    }
    
    public boolean doHandshake(SocketChannel sc) throws IOException {
        return true;
    }
    
    protected void handleData(SocketChannel channel) throws IOException {
        this.doHandshake(channel);
        super.handleData(channel);
    }
}

package it.saiv.bread.net;

import it.saiv.bread.event.Emitter;
import it.saiv.bread.event.Loop;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

import javax.net.ssl.SSLContext;

public class Server extends Emitter implements Runnable {
    protected Loop<SelectableChannel> loop;
    protected ServerSocketChannel master;
    protected SSLContext sslContext;

    public Server(Loop<SelectableChannel> loop) {
        this(loop, null);
    }

    public Server(Loop<SelectableChannel> loop, SSLContext sslContext) {
        this.loop = loop;
        this.sslContext = sslContext;
    }

    public Server listen(int port) {
        return this.listen(new InetSocketAddress(port));
    }

    public Server listen(int port, String host) {
        return this.listen(new InetSocketAddress(host, port));
    }

    public Server listen(int port, InetAddress inetAddress) {
        return this.listen(new InetSocketAddress(inetAddress, port));
    }

    public Server listen(InetSocketAddress inetSocketAddress) {
        try {
            this.master = ServerSocketChannel.open();
            this.master.configureBlocking(false);
            this.loop.addAcceptChannel(this.master, (SelectableChannel serverSocketChannel) -> {
                ServerSocketChannel master = (ServerSocketChannel) serverSocketChannel;
                try {
                    SocketChannel channel = master.accept();
                    this.handleConnection(channel);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            });
            this.master.socket().setReuseAddress(true);
            this.master.bind(inetSocketAddress);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return this;
    }

    public void shutdown() {
        this.loop.removeChannel(this.master);
        try {
            this.master.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void run() {
        this.loop.run();
    }

    protected void handleConnection(SocketChannel channel) {
        try {
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        Connection connection;

        if (null != this.sslContext) {
            connection = new SecureConnection(channel, this.loop, this.sslContext);
        } else {
            connection = new Connection(channel, this.loop);
        }
        
        connection.resume();

        this.emit("connection", new AcceptedConnectionEvent(connection));
    }
}

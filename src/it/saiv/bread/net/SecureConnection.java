package it.saiv.bread.net;

import it.saiv.bread.event.Loop;
import it.saiv.bread.streaming.DataEvent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import java.security.cert.X509Certificate;

public class SecureConnection extends Connection {
    
    private SSLEngine sslEngine;

    private int appBBSize;
    private int netBBSize;

    private ByteBuffer inNetBB;
    private ByteBuffer outNetBB;

    private static ByteBuffer hsBB = ByteBuffer.allocate(0);

    private HandshakeStatus initialHSStatus;
    private boolean initialHSComplete;

    private boolean shutdown = false;

    public SecureConnection(SocketChannel channel, Loop<SelectableChannel> loop, SSLContext sslContext) {
        super(channel, loop);

        this.sslEngine = sslContext.createSSLEngine();
        this.sslEngine.setUseClientMode(false);
        this.initialHSStatus = HandshakeStatus.NEED_UNWRAP;
        this.initialHSComplete = false;

        this.netBBSize = this.sslEngine.getSession().getPacketBufferSize();
        this.inNetBB = ByteBuffer.allocate(this.netBBSize);
        this.outNetBB = ByteBuffer.allocate(this.netBBSize);
        this.outNetBB.position(0);
        this.outNetBB.limit(0);

        this.appBBSize = this.sslEngine.getSession().getApplicationBufferSize();
        this.requestBB = ByteBuffer.allocate(this.appBBSize);
    }

    protected void resizeRequestBB() {
        super.resizeRequestBB(this.appBBSize);
    }

    private void resizeResponseBB() {
        ByteBuffer bb = ByteBuffer.allocate(this.netBBSize);
        this.inNetBB.flip();
        bb.put(this.inNetBB);
        this.inNetBB = bb;
    }

    private boolean tryFlush(ByteBuffer bb) throws IOException {
        super.write(bb);
        return !bb.hasRemaining();
    }

    public boolean doHandshake(SocketChannel sc) throws IOException {
        
        SSLEngineResult result;
        
        if (this.initialHSComplete) {
            return this.initialHSComplete;
        }
        
        if (this.outNetBB.hasRemaining()) {
            if (!this.tryFlush(this.outNetBB)) {
                return false;
            }
            
            switch (this.initialHSStatus) {
            case FINISHED:
                this.initialHSComplete = true;
            case NEED_UNWRAP:
                // TODO OP_READ
                break;
            case NEED_WRAP:
                break;
            case NOT_HANDSHAKING:
                break;
            default:
                // TODO invalid state?
            }
        }
        
        switch (this.initialHSStatus) {
        case NEED_UNWRAP:
            if (sc.read(this.inNetBB) == -1) {
                this.sslEngine.closeInbound();
                return this.initialHSComplete;
            }
            
            needIO: while (this.initialHSStatus == HandshakeStatus.NEED_UNWRAP) {
                this.resizeRequestBB();
                this.inNetBB.flip();
                result = this.sslEngine.unwrap(this.inNetBB, this.requestBB);
                this.inNetBB.compact();
                
                this.initialHSStatus = result.getHandshakeStatus();
                
                switch (result.getStatus()) {
                case BUFFER_OVERFLOW:
                    this.appBBSize = this.sslEngine.getSession().getApplicationBufferSize();
                    break;
                case BUFFER_UNDERFLOW:
                    this.netBBSize = this.sslEngine.getSession().getPacketBufferSize();
                    if (this.netBBSize > this.inNetBB.capacity()) {
                        this.resizeResponseBB();
                    }
                    // TODO OP_READ
                    break needIO;
                case OK:
                    switch (this.initialHSStatus) {
                    case FINISHED:
                        this.initialHSComplete = true;
                        break needIO;
                    case NEED_TASK:
                        this.initialHSStatus = null; // TODO doTasks();
                        break;
                    case NEED_UNWRAP:
                        break;
                    case NEED_WRAP:
                        break;
                    case NOT_HANDSHAKING:
                        throw new IOException("Not handshaking during initial handshake");
                    default:
                        // TODO invalid state?
                    }
                    break;
                default:
                    throw new IOException("Received " + result.getStatus() + " during initial handshaking");
                }
            }
            
            if (this.initialHSStatus != HandshakeStatus.NEED_WRAP) {
                break;
            }
        case NEED_WRAP:
            this.outNetBB.clear();
            result = this.sslEngine.wrap(SecureConnection.hsBB, this.outNetBB);
            this.outNetBB.flip();
            
            this.initialHSStatus = result.getHandshakeStatus();
            
            switch (result.getStatus()) {
            case OK:
                if (this.initialHSStatus == HandshakeStatus.NEED_TASK) {
                    this.initialHSStatus = null; // TODO doTasks();
                }
                // TODO OP_WRITE
                break;
            default:
                throw new IOException("Received " + result.getStatus() + " during initial handshaking");
            }
            break;
        default:
            throw new RuntimeException("Invalid handshaking state " + this.initialHSStatus);
        }
        
        return this.initialHSComplete;
    }
    
    protected void handleData(ReadableByteChannel channel) throws IOException {
        SSLEngineResult result;
        
        if (this.initialHSComplete) {
            throw new IllegalStateException();
        }
        
        //int pos = this.requestBB.position();
        
        if (channel.read(this.inNetBB) == -1) {
            this.sslEngine.closeInbound();
            return;
        }
        
        do {
            this.resizeRequestBB();
            this.inNetBB.flip();
            result = this.sslEngine.unwrap(this.inNetBB, this.requestBB);
            this.inNetBB.compact();
            
            switch (result.getStatus()) {
            case BUFFER_OVERFLOW:
                this.appBBSize = this.sslEngine.getSession().getApplicationBufferSize();
                break;
            case BUFFER_UNDERFLOW:
                this.netBBSize = this.sslEngine.getSession().getPacketBufferSize();
                if (this.netBBSize > this.inNetBB.capacity()) {
                    this.resizeResponseBB();
                    break;
                }
            case OK:
                if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                    // TODO doTasks();
                }
                break;
            default:
                throw new IOException("SSLEngine error during data read: " + result.getStatus());
            }
        } while ((this.inNetBB.position() != 0) && result.getStatus() != Status.BUFFER_UNDERFLOW);
        
        this.emit("data", new DataEvent(this.requestBB));
        
        //return (this.requestBB.position() - pos);
    }
    
    public boolean shutdown() throws IOException {
        if (!this.shutdown) {
            this.sslEngine.closeOutbound();
            this.shutdown = true;
        }
        
        if (this.outNetBB.hasRemaining() && this.tryFlush(this.outNetBB)) {
            return false;
        }
        
        this.outNetBB.clear();
        SSLEngineResult result = this.sslEngine.wrap(SecureConnection.hsBB, this.outNetBB);
        if (result.getStatus() != Status.CLOSED) {
            throw new SSLException("Improper close state");
        }
        this.outNetBB.flip();
        
        if (this.outNetBB.hasRemaining()) {
            this.tryFlush(this.outNetBB);
        }
        
        return (!this.outNetBB.hasRemaining() && (result.getHandshakeStatus() != HandshakeStatus.NEED_WRAP));
    }

    public boolean isSecure() {
        return true;
    }

    public boolean isClientIdentified() {
        return false;
    }

    public X509Certificate getClientIdentity() {
        return null;
    }

    public X509Certificate getServerIdentity() {
        return null;
    }
}

package it.saiv.bread.streaming;

import it.saiv.bread.event.Emitter;
import it.saiv.bread.event.Loop;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.ByteBuffer;
import java.io.IOException;

public class Stream<C extends Channel> extends Emitter {
    public static final int BUFFER_SIZE = 4096;

    protected C channel;
    protected Loop<C> loop;
    protected ByteBuffer requestBB;
    protected ByteBuffer responseBB;
    protected boolean listening;
    protected boolean readable;
    protected boolean writable;
    protected boolean closing;

    public Stream(C channel, Loop<C> loop) {
        this.channel = channel;
        this.loop = loop;
        this.listening = false;
        this.readable = true;
        this.writable = true;
        this.closing = false;
        this.requestBB = ByteBuffer.allocate(Stream.BUFFER_SIZE);
        this.responseBB = ByteBuffer.allocate(Stream.BUFFER_SIZE);
    }

    public boolean isReadable() {
        return this.readable;
    }

    public boolean isWritable() {
        return this.writable;
    }

    public void pause() {
        this.loop.removeReadChannel(this.channel);
    }

    public void resume() {
        try {
            this.loop.addReadChannel(this.channel, (C channel) -> {
                ReadableByteChannel readableByteChannel = (ReadableByteChannel) channel;
                try {
                    this.handleData(readableByteChannel);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            });
        } catch (ClosedChannelException cce) {
            cce.printStackTrace();
        }
    }

    public void write(String string) {
        this.write(string.getBytes());
    }

    public void write(byte[] data) {
        this.write(ByteBuffer.wrap(data));
    }
    
    public void write(ByteBuffer data) {
        if (!this.writable) {
            // TODO Log trying to write to a non-writable stream
            return;
        }

        this.responseBB.put(data);

        if (!this.listening) {
            this.listening = true;
            try {
                this.loop.addWriteChannel(this.channel, (C channel) -> {
                    WritableByteChannel writableByteChannel = (WritableByteChannel) channel;
                    // TODO if SSL already handled?
                    try {
                        this.handleWrite(writableByteChannel);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    if (0 == this.responseBB.remaining()) {
                        this.loop.removeWriteChannel(channel);
                        this.listening = false;
                        this.emit("full-drain");
                    }
                });
            } catch (ClosedChannelException cce) {
                cce.printStackTrace();
            }
        }
    }

    public void end(byte[] data) {
        this.write(data);
        this.end();
    }

    public void end() {
        if (!this.writable) {
            return;
        }

        this.closing = true;
        this.readable = false;
        this.writable = false;
        this.emit("end", new EndEvent<C>(this));
    }

    public void close() throws IOException {
        this.readable = false;
        this.writable = false;
        this.emit("close", new CloseEvent<C>(this));
        this.loop.removeChannel(this.channel);
        this.channel.close();
        this.removeAllListeners();
    }
    
    protected void handleData(ReadableByteChannel channel) throws IOException {
        int numRead = 0;
        try {
            numRead = channel.read(this.requestBB);
        } catch (IOException ioe) {
            channel.close();
            ioe.printStackTrace();
        }
        
        if (numRead >= 0) {
            this.emit("data", new DataEvent(this.requestBB));
        } else {
            channel.close();
        }
        
        if (!channel.isOpen()) {
            this.end();
        }
    }

    protected void handleWrite(WritableByteChannel channel) throws IOException {
        int numWritten = 0;
        try {
            System.err.println("Writing");
            this.responseBB.flip();
            numWritten = channel.write(this.responseBB);
            this.responseBB.compact();
            System.err.println("Written " + numWritten + " bytes.");
            System.err.println("Remaining " + this.responseBB.remaining() + " bytes.");
        } catch (IOException ioe) {
            channel.close();
            ioe.printStackTrace();
        }
    }

    protected void resizeRequestBB(int remaining) {
        if (this.requestBB.remaining() < remaining) {
            ByteBuffer bb = ByteBuffer.allocate(this.requestBB.capacity() * 2);
            this.requestBB.flip();
            bb.put(this.requestBB);
            this.requestBB = bb;
        }
    }
}

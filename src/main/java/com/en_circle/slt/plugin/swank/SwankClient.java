package com.en_circle.slt.plugin.swank;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SwankClient implements AutoCloseable, Runnable {
    private static final AtomicInteger TC = new AtomicInteger();

    private Socket connection;

    private final String host;
    private final int port;
    private final SwankReply callback;
    private final Thread readThread;

    private ExecutorService swankExecutor;

    public SwankClient(String host, int port, SwankReply callback) {
        this.swankExecutor = Executors.newCachedThreadPool();
        this.host = host;
        this.port = port;
        this.callback = callback;

        readThread = new Thread(this);
        readThread.setDaemon(true);
        readThread.setName("SWANK ReadThread " + TC.addAndGet(1));
        readThread.start();
    }

    private synchronized void checkConnection() throws Exception {
        if (connection != null) {
            if (connection.isConnected()) {
                return;
            }
            connection.close();
        }

        connection = new Socket(host, port);
        connection.setKeepAlive(true);
    }

    public void swankSend(SlimePacket value) {
        swankExecutor.submit(() -> {
            try {
                checkConnection();
                value.writeTo(connection.getOutputStream());
            } catch (Exception ignored) {
                // TODO: maybe log
            }
        });
    }

    @Override
    public void close() throws Exception {
        swankExecutor.shutdown();

        if (connection != null) {
            connection.close();
        }

        while (readThread.isAlive()) {
            readThread.interrupt();
        }
        readThread.join();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                checkConnection();
                InputStream is = connection.getInputStream();
                SlimePacket packet = SlimePacket.fromInput(is);
                callback.onSwankMessage(packet);
            } catch (ConnectException | InterruptedException e) {
                return;
            } catch (IOException e) {
                if (e instanceof EOFException) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface SwankReply {
        void onSwankMessage(SlimePacket packet);
    }

}

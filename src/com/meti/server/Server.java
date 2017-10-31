package com.meti.server;

import com.meti.server.asset.AssetManager;
import com.meti.server.util.Command;
import com.meti.util.Activator;
import com.meti.util.Loop;
import com.meti.util.Stoppable;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import static com.meti.Main.getInstance;

/**
 * @author SirMathhman
 * @version 0.0.0
 * @since 10/20/2017
 */
public class Server implements Stoppable {
    private final Commander commander = new Commander();

    private final AssetManager manager = new AssetManager();
    private final ServerSocket serverSocket;

    private final List<ClientLoop> clientLoops = new ArrayList<>();
    private final List<Socket> sockets = new ArrayList<>();

    private final ListenLoop listenLoop = new ListenLoop(this);
    private final DisconnectLoop disconnectLoop = new DisconnectLoop();
    private final ClientExecutor executor = new ClientExecutor();
    private final File directory = new File("Nexus");

    private Activator<Socket> onClientConnect;
    private Activator<Socket> onClientDisconnect;
    private String password;

    public Server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void stop() {
        listenLoop.setRunning(false);

        for (Loop loop : clientLoops) {
            loop.setRunning(false);
        }

        try {
            serverSocket.close();

            for (Socket socket : sockets) {
                socket.close();
            }
        } catch (IOException e) {
            getInstance().log(Level.SEVERE, e);
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void listen() throws IOException, InstantiationException, IllegalAccessException {
        getInstance().log(Level.INFO, "Reading directory");

        manager.load();
        manager.read(directory);

        getInstance().log(Level.INFO, "Listening for clients");

        new Thread(listenLoop).start();
        new Thread(disconnectLoop).start();
    }

    public void setOnClientConnect(Activator<Socket> onClientConnect) {
        this.onClientConnect = onClientConnect;
    }

    public void setOnClientDisconnect(Activator<Socket> onClientDisconnect) {
        this.onClientDisconnect = onClientDisconnect;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AssetManager getAssetManager() {
        return manager;
    }

    public void runCommand(Command command) throws IOException {
        commander.runCommand(command);
    }

    public class DisconnectLoop extends Loop {
        @Override
        public void loop() {
            for (Socket socket : sockets) {
                if (socket.isClosed()) {
                    //theoretically, this should work
                    //even though socket has been closed, still has attributes
                    //not sure if this should be a safety concern
                    onClientDisconnect.activate(socket);
                    sockets.remove(socket);
                }
            }
        }
    }

    public class ListenLoop extends Loop {
        private final Server parent;

        ListenLoop(Server parent) {
            this.parent = parent;
        }

        @Override
        public void loop() {
            try {
                //may throw a socket exception while the loop is listening
                Socket socket = serverSocket.accept();

                getInstance().log(Level.INFO, "Located client at " + socket.getInetAddress());

                sockets.add(socket);
                executor.execute(new ClientLoop(parent, socket, clientLoops.toArray(new ClientLoop[0])));

                onClientConnect.activate(socket);
            } catch (SocketException e) {
                setRunning(false);
            } catch (IOException e) {
                getInstance().log(Level.WARNING, e);
            }
        }
    }

    public class ClientExecutor implements Executor {

        @Override
        public void execute(Runnable runnable) {
            if (runnable instanceof ClientLoop) {
                ClientLoop command = (ClientLoop) runnable;

                clientLoops.forEach(clientLoop -> clientLoop.getClientLoops().add(command));

                clientLoops.add(command);
            } else {
                getInstance().log(Level.WARNING, "Located object of not subtype Loop");
            }

            //make sure we execute the thread
            new Thread(runnable).start();
        }
    }
}

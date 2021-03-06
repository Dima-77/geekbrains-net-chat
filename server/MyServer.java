
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

public class MyServer {

    private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

    public MyServer() {
        ServerSocket server = null;
        Socket s = null;
        final int CLIENT_AUTH_TIMEOUT = 300;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        HashSet<ClientHandler> hss = new HashSet<>();
                        for (ClientHandler o : clients) {
                            if (o.getName().isEmpty()) {
                                o.setAuthTimer(o.getAuthTimer() + 1);
                                if (o.getAuthTimer() > CLIENT_AUTH_TIMEOUT) {
                                    hss.add(o);
                                }
                            }
                        }
                        for (ClientHandler o : hss) {
                            o.close();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        try {
            server = new ServerSocket(8189);
            System.out.println("Server created. Waiting for client...");
            while (true) {
                s = server.accept();
                System.out.println("Client connected");
                ClientHandler h = new ClientHandler(s, this);
                clients.add(h);
                new Thread(h).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
                System.out.println("Server closed");
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void remove(ClientHandler o) {
        clients.remove(o);
    }

    public boolean isNicknameUsed(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick))
                return true;
        }
        return false;
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            if (!o.getName().isEmpty())
                o.sendMsg(msg);
        }
    }

    public void removeAll() {
        clients.clear();
    }

    public boolean removeByNick(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) {
                o.sendMsg("end session");
                remove(o);
                return true;
            }
        }
        return false;
    }
}

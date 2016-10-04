
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private MyServer owner;
    private Socket s;
    private DataOutputStream out;
    private DataInputStream in;
    private String name;
    private String login;
    private int authTimer;
    private boolean isAdmin = false;

    public int getAuthTimer() {
        return authTimer;
    }

    public void setAuthTimer(int authTimer) {
        this.authTimer = authTimer;
    }

    public String getName() {
        return name;
    }

    public ClientHandler(Socket s, MyServer owner) {
        try {
            this.s = s;
            this.owner = owner;
            out = new DataOutputStream(s.getOutputStream());
            in = new DataInputStream(s.getInputStream());
            name = "";
            authTimer = 0;
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String w = in.readUTF();
                if (w != null) {
                    String[] n = w.split("\t");
                    if (n.length == 3) {
                        String t = SQLHandler.getNickByLoginPassword(n[1], n[2]);
                        String[] errType = t.split(":");
                        login = n[1];
                        if (!owner.isNicknameUsed(t) && !errType[0].equals("Auth error")) {
                            owner.broadcastMsg(t + " connected to the chatroom");
                            name = t;
                            sendMsg("zxcvb");
                            sendMsg("Server: type /help to see available commands");
                            break;
                        } else {
                            if (errType[0].equals("Auth error"))
                                sendMsg(t);
                            if (owner.isNicknameUsed(t))
                                sendMsg("Auth Error: Account are busy");
                        }
                    }
                }
                Thread.sleep(100);
            }
            while (true) {
                String w = in.readUTF();
                if (executeCommand(w)) {
                    continue;
                }
                if (w != null) {
                    if (w.equalsIgnoreCase("END")) {
                        //Отправляем команду на завешение потока клиента
                        sendMsg("end session");
                        break;
                    }
                    owner.broadcastMsg(name + ": " + w);
                    SQLHandler.saveMsg(login, name, w);
                    System.out.println(name + ": " + w);
                }
                Thread.sleep(100);
            }
        } catch (IOException e) {
            System.out.println("IOException");
        } catch (InterruptedException e) {
            System.out.println("Thread sleep error");
        } finally {
            close();
        }
    }

    public void close() {
        try {
            System.out.println("Client disconnected");
            owner.remove(this);
            s.close();
            if (!name.isEmpty())
                owner.broadcastMsg(name + " disconnected from the chatroom");
        } catch (IOException e) {
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
        }
    }

    private boolean executeCommand(String w) {
        if ("/".equals(w.substring(0, 1))) {
            sendMsg(w);
            String[] com = w.split(" ");
            switch (com[0]) {
                case ("/changenick"):
                    if (com.length > 1 && SQLHandler.setNick(login, name, com[1])) {
                        sendMsg("Server: now your name is " + com[1]);
                        name = com[1];
                        break;
                    } else {
                        sendMsg("Server: illegal expression");
                        return false;
                    }
                case "/gethistory":
                    String history;
                    if (com.length == 1) {
                        history = SQLHandler.getHistory();
                    } else {
                        history = SQLHandler.getHistory(com[1]);
                    }
                    if (history != null) {
                        sendMsg(history);
                        break;
                    } else {
                        sendMsg("Server: nothing found");
                        return false;
                    }
                case "/help":
                    if (com.length == 1) {
                        sendMsg("Available commands:\n" +
                                "/changenick NEWNICK\n" +
                                "/gethistory [NICKNAME]\n" +
                                "/admin\n\n" +
                                "Where first word is command,\n" +
                                "second word is parameter,\n" +
                                "parameter in braces is optional,\n" +
                                "all words are case sensitive");
                        break;
                    }
                case "/admin":
                    if (com.length == 1) {
                        sendMsg("Server: type password");
                        try {
                            String password = in.readUTF();
                            if (password.equals("1111")) {
                                isAdmin = true;
                                sendMsg("Server: god mode on");
                                sendMsg("Available commands:\n" +
                                        "/kick NICKNAME\n" +
                                        "/kick all\n");
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //Команды доступные админу:
                case "/kick":
                    if (isAdmin) {
                        if (com.length == 2) {
                            if (com[1].equals("all")) {
                                owner.broadcastMsg("end session");
                                owner.removeAll();
                                break;
                            } else {
                                if (owner.removeByNick(com[1])) break;
                            }
                        }
                    }
                default:
                    sendMsg("Server: illegal expression");
                    return false;
            }
            return true;
        }
        return false;
    }
}

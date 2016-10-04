
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class MyWindow extends JFrame {

    JTextField jtf;
    JTextArea jta;

    final String SERVER_ADDR = "localhost";
    final int SERVER_PORT = 8189;
    Socket sock;
    DataInputStream in;
    DataOutputStream out;
    boolean authorized = false;

    public MyWindow() {
        setBounds(600, 300, 500, 500);
        setTitle("Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jta = new JTextArea();
        jta.setEditable(false);
        jta.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jta);
        add(jsp, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel authPanel = new JPanel(new GridLayout());

        JTextField jtfLogin = new JTextField();
        JTextField jtfPass = new JTextField();
        JButton jbAuth = new JButton("Auth");
        authPanel.add(jtfLogin);
        authPanel.add(jtfPass);
        authPanel.add(jbAuth);
        jtfLogin.setToolTipText("Login");
        add(authPanel, BorderLayout.NORTH);

        jbAuth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sock == null || sock.isClosed()) {
                    System.out.println("TRY");
                    connectToServer();
                } else if (sock.isConnected()) {
                    //Перед повторной авторизацией корректно выходим
                    try {
                        out.writeUTF("END");
                        out.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println("TRY");
                    connectToServer();
                }
                sendAuthCmd("auth\t" + jtfLogin.getText() + "\t" + jtfPass.getText());
            }
        });

        add(bottomPanel, BorderLayout.SOUTH);
        JButton jbSend = new JButton("SEND");
        bottomPanel.add(jbSend, BorderLayout.EAST);
        jtf = new JTextField();
        bottomPanel.add(jtf, BorderLayout.CENTER);

        jbSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!jtf.getText().trim().isEmpty()) {
                    sendMsg();
                    jtf.grabFocus();
                }
            }
        });

        jtf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMsg();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                try {
                    out.writeUTF("end");
                    out.flush();
                } catch (IOException exc) {
                }
            }
        });

        setVisible(true);
    }

    public void connectToServer() {
        try {
            sock = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String w = in.readUTF();
                        if (w != null) {
                            if (w.equals("zxcvb")) {
                                jta.setText("Connected\n");
                                authorized = true;
                                break;
                            } else {
                                jta.append(w);
                                jta.append("\n");
                            }
                        }
                    }

                    while (true) {
                        String w = in.readUTF();
                        if (w != null) {
                            if (w.equalsIgnoreCase("end session")) break;
                            jta.append(w);
                            jta.append("\n");
                            jta.setCaretPosition(jta.getDocument().getLength());
                        }
                        Thread.sleep(100);
                    }
                } catch (SocketException e) {
                    authorized = false;
                    System.out.println("Socket closed");
                } catch (Exception e) {
                    authorized = false;
                    System.out.println("ERR");
                } finally {
                    try {
                        out.close();
                        in.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } finally {
                        try {
                            sock.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        }).start();
    }

    public void sendAuthCmd(String str) {
        try {
            out.writeUTF(str);
            out.flush();
        } catch (IOException e) {
            System.out.println("Send auth error");
        }
    }

    public void sendMsg() {
        if (authorized) {
            try {
                String a = jtf.getText();
                out.writeUTF(a);
                out.flush();
                jtf.setText("");
            } catch (IOException e) {
                System.out.println("Send msg error");
            }
        }
    }
}

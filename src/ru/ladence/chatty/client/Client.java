package ru.ladence.chatty.client;

import ru.ladence.chatty.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

class Client {
    private JTextField messageArea;
    private JButton sendButton;
    private JTextArea chattyArea;
    private JFrame frame;

    // Net fields
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private Socket socket;

    private void initUi() {
        frame = new JFrame("Chatty");
        messageArea = new JTextField(12);
        sendButton = new JButton("Send message");
        chattyArea = new JTextArea(12,12 );
        chattyArea.setEditable(false);

        frame.getContentPane().add(chattyArea, BorderLayout.PAGE_START);
        frame.getContentPane().add(messageArea, BorderLayout.CENTER);
        frame.getContentPane().add(sendButton, BorderLayout.PAGE_END);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    dataOutputStream.writeUTF("/exit");
                } catch (IOException e1) {
                    System.out.println("Can't send exit comma!");
                }

                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    dataInputStream.close();
                    dataOutputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        frame.pack();
        frame.setVisible(true);

        sendButton.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String message = messageArea.getText();

                    try {
                        dataOutputStream.writeUTF(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (message.toLowerCase().equals("/exit")) {
                        System.exit(1);
                    }
                    return null;
                }
                @Override
                protected void done() {
                    messageArea.setText("");
                }
            }.execute();
        });

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String currentMessage;

                while (true) {
                    try {
                        currentMessage = dataInputStream.readUTF();
                        chattyArea.append(currentMessage + "\n");
                    } catch (IOException ex) {
                        System.out.println("Can't receive message from the server!");
                        break;
                    }
                }
                return null;
            }
        }.execute();

    }

    private void initNet() throws IOException {
        socket = new Socket(InetAddress.getByName("localhost"), Config.PORT);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    private Client() {
        try {
            initNet();
            initUi();
        } catch (IOException e) {
            System.out.println("Can't establish connection to server!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }
}


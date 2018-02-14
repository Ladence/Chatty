package ru.ladence.chatty.client;

import ru.ladence.chatty.Config;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

class Client extends JFrame {
    private JTextField messageArea;
    private JButton sendButton;
    private JTextArea chattyArea;

    // Net fields
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    private void initUi() {
        messageArea = new JTextField(12);
        sendButton = new JButton("Send message");
        chattyArea = new JTextArea(12,12 );

        this.getContentPane().add(chattyArea, BorderLayout.PAGE_START);
        this.getContentPane().add(messageArea, BorderLayout.CENTER);
        this.getContentPane().add(sendButton, BorderLayout.PAGE_END);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();

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

        SwingWorker<Void, Void> receiveMessageWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String currentMessage = null;

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
        };

        receiveMessageWorker.execute();
    }

    private void initNet() throws IOException {
        Socket socket = new Socket(InetAddress.getByName("localhost"), Config.PORT);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    private Client() {
        super("Chatty");
        try {
            initNet();
            initUi();
        } catch (IOException e) {
            System.out.println("Can't establish connection to server!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client client = new Client();
            client.setVisible(true);
        });
    }
}


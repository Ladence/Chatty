package ru.ladence.chatty.server;

import ru.ladence.chatty.ConnectionConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class Server {
    private List<ClientHandler> clients;

    private Server() {
        clients = new ArrayList<>();
        try {
            ServerSocket serverSocket = new ServerSocket(ConnectionConstants.PORT);
            Socket clientSocket;

            while (true) {
                clientSocket = serverSocket.accept();

                System.out.println("There is a new client! " + clientSocket);

                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                ClientHandler clientHandler = new ClientHandler(String.valueOf(clients.size()), dataInputStream, dataOutputStream, clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Can't establish connection!");
        }
    }

    public static void main(String[] args) {
        new Server();
    }

    private class ClientHandler implements Runnable {
        private String name;
        private final DataInputStream dataInputStream;
        private final DataOutputStream dataOutputStream;
        private Socket socket;

        private ClientHandler(String name, DataInputStream dataInputStream, DataOutputStream dataOutputStream, Socket socket) {
            this.name = name;
            this.dataInputStream = dataInputStream;
            this.dataOutputStream = dataOutputStream;
            this.socket = socket;
        }

        private void userLogout() {
            System.out.println("Client : " + socket + " has been logout.");
            clients.remove(this);
        }

        @Override
        public void run() {
            String received;
            while (true) {
                try {
                    received = dataInputStream.readUTF();
                    System.out.println(name + " : " + received);

                    if (received.toLowerCase().equals("/exit")) {
                        userLogout();
                        socket.close();
                        break;
                    }

                    for (ClientHandler client : clients) {
                        client.dataOutputStream.writeUTF(name + " : " + received);
                    }

                } catch (IOException e) {
                    System.out.println(clients.size());
                }
            }

            try {
                dataInputStream.close();
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

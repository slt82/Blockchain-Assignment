package com.clientApp;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class JavaClient
{
    final static int Port = 1234;

    public static void main(String args[]) throws UnknownHostException, IOException
    {
        try {

            Scanner scan = new Scanner(System.in);

            // gets the localhost IP address
            InetAddress ip = InetAddress.getByName("localhost");

            // creates socket connection
            Socket s = new Socket(ip, Port);

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // client thread for sending commands
            Thread sendMessage = new Thread(new Runnable()
            {
                @Override
                public void run() {
                    while (true) {

                        // read the message to deliver.
                        String message = scan.nextLine();

                        try {
                            // send message on data out
                            dos.writeUTF(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            // client thread for receiving data from the server
            Thread readMessage = new Thread(new Runnable()
            {
                @Override
                public void run() {

                    while (true) {
                        try {
                            // prints the server response
                            String message = dis.readUTF();
                            System.out.println(message);
                        } catch (IOException e) {

                            System.exit(0);
                        }
                    }
                }
            });

            sendMessage.start();
            readMessage.start();

        }catch(Exception e) {

            System.out.println("Connection Closed.");
        }
    }
} 
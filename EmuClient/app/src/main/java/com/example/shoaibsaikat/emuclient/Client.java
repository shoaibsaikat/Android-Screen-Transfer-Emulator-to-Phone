package com.example.shoaibsaikat.emuclient;

import java.net.*;
import java.io.*;

public class Client extends Thread {

    private ImageData caller;
    private String hostName;
    private int portNumber;

    private DataOutputStream out;
    private DataInputStream in;
    private Socket s = null;

    private static final long TIMEOUT = 100;
    private static final int PACKET_LEN = 1024;
    private static final String HEADER = "$HEADER:";

    Client(ImageData caller, String addr, int port) {
        this.caller = caller;
        hostName = addr;
        portNumber = port;
    }

    private boolean isAnEmptyArray(byte []array) {
        for(byte b : array)
            if(b != 0)
                return false;
        return true;
    }

    public void sendMessage(final byte[] msg) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    int count = 0;
                    int length = msg.length;
                    byte[] writeBytes;
                    // writing length of the picture
                    String headerMsg = HEADER + length + "$";
                    out.write(headerMsg.getBytes());

                    do {
                        writeBytes = new byte[PACKET_LEN];
                        System.arraycopy(msg, count, writeBytes, 0, PACKET_LEN);
                        out.write(writeBytes);
                        count += PACKET_LEN;
                    } while (length - count >= PACKET_LEN);

                    // last packet
                    if(length - count > 0) {

                        writeBytes = new byte[PACKET_LEN];
                        System.arraycopy(msg, count, writeBytes, 0, length - count);
                        out.write(writeBytes);
                    }
                    Thread.sleep(TIMEOUT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private int readImageLength(String header) {
        int i = HEADER.length();
        while (header.charAt(i) != '$')
            i++;
        String lenString = header.substring(HEADER.length(), i);
        return Integer.parseInt(lenString);
    }

    public int readImage(int length) {
        byte[] readBytes;
        int count = 0;
        byte image[] = new byte[length];

        try {
            do {
                readBytes = new byte[PACKET_LEN];
                in.read(readBytes);

                if(isAnEmptyArray(readBytes))
                    continue;

                System.arraycopy(readBytes, 0, image, count, PACKET_LEN);
                count += PACKET_LEN;
            } while (length - count >= PACKET_LEN);

            // last packet and length < PACKET_LEN
            if(length - count > 0) {
                while(true) {
                    readBytes = new byte[PACKET_LEN];
                    in.read(readBytes);

                    if(isAnEmptyArray(readBytes))
                        continue;

                    // call Activity to draw image
                    System.arraycopy(readBytes, 0, image, count, length - count);
                    break;
                }
            }
            caller.drawImage(image, image.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void run() {
        try {
            s = new Socket(hostName, portNumber);
            out = new DataOutputStream(s.getOutputStream());
            in = new DataInputStream(s.getInputStream());

            int length;
            String header;

            while (true) {

                byte[] readBytes = new byte[PACKET_LEN];
                // read header and then image packets
                in.read(readBytes);

                if(isAnEmptyArray(readBytes))
                    continue;
                header = new String(readBytes);

                if(header.startsWith(HEADER)) {
                    length = readImageLength(header);
                    readImage(length);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
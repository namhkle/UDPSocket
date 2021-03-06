import java.net.*;    // InetAddress, DatagramSocket, DatagramPacket, UnknownHostException, SocketException
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UDPServer {
    public static void main(String[] args) throws IOException {

        int serverPort = 3537;    // Port number that server will be given
        DatagramSocket serverSocket;    // Defines the serverSocket object
        int incorrectFile = 0;
        try {
            serverSocket = new DatagramSocket(serverPort);   // Creates a new Datagram socket and binds it to port 3535
        } catch (SocketException e) { // Assigns port number to any open port number on machine if specified port number above is taken

            System.out.println("Was unable to create socket with port number - " + serverPort + ".");

            incorrectFile++;  //count the number of time that incurs incorrect transfer

            System.out.println("Auto-assigning port number.");
            serverSocket = new DatagramSocket();   // Creates a new Datagram socket and binds it to an open port on machine
            System.out.println("New port number is - " + serverSocket.getPort());

        }

        byte[] receiveBuffer = new byte[1024];    // Creates a buffer for received packet data at 65 KB
        byte[] sendBuffer = new byte[1024];    // Used for sending packets back to client
        byte[] dataBuffer;
        boolean running = true;   // for while loop that will run until client is done
        int sendAmount = 100;
        long totalTime = 0;
        for (int i = 0; i <= sendAmount - 1; i++) {
            int corruptFiles = 0;   // Will hold number of currupted files
            int correctFiles = 0;   // Will hold number of correct files
            int filesReceived = 0;    // Will hold number of files received

            String fileName = "Test1 - large.txt";
            File receivedFile = new File(fileName);
            boolean finishedReceiving = false;
            FileOutputStream stream = new FileOutputStream(receivedFile);   // Used to write new file based on data being received from client

            System.out.println("\nI am ready for any client side request...");
            System.out.println("I am receiving file for the " + (filesReceived + i + 1) + "th time");
            long startTime = System.nanoTime();

            while (!finishedReceiving) {
                DatagramPacket receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);    // Creates socket that will receive packets the length of buffer
                serverSocket.receive(receivedPacket);   // Waits for a packet to be received from Client and stores it in receivedPacket when it arrives
                // Process data received with 0 offset for the length of the data to see if the packet received was a part of the file
                if (receivedPacket.getLength() < 1024) {

                    finishedReceiving = true;
                    int packetLength = receivedPacket.getLength();    // Get length of packet since it can vary
                    dataBuffer = receivedPacket.getData();    // Get data from packet regardless of packet size
                    dataBuffer = Arrays.copyOfRange(dataBuffer, 0, packetLength);   // Retrieve only the bytes that contain data using packetLength
                    stream.write(dataBuffer);
                    stream.flush();   // Flush output stream
                    stream.close();   // Close output stream

                    InetAddress clientAddress = receivedPacket.getAddress();    // Retrieves address from received packet to send any information back
                    int port = receivedPacket.getPort();    // Retrieves port number of client from received packet
                    sendBuffer = "File Received".getBytes();    // Turns return messgae into byte[] to send back to client

                    DatagramPacket sendingPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, port);    // Creates packet with message to be sent back to client
                    serverSocket.send(sendingPacket);

                    long endTime = System.nanoTime();

                    System.out.println("I am finsihed receiving file " + fileName + " for the " + (filesReceived + i + 1) + "th time.");
                    System.out.println("The time used in millisecond to receive " + fileName + " for the " + (filesReceived + i + 1) + "th time is: " + ((endTime - startTime) / 1000000));

                    totalTime += (endTime - startTime) / 1000000;
                } else {

                    int packetLength = receivedPacket.getLength();    // Get length of packet since it can vary
                    dataBuffer = receivedPacket.getData();    // Get data from packet regardless of packet size
                    dataBuffer = Arrays.copyOfRange(dataBuffer, 0, packetLength);   // Retrieve only the bytes that contain data using packetLength
                    stream.write(dataBuffer);   // Write data that was retrieved to file being written for comparison

                    InetAddress clientAddress = receivedPacket.getAddress();    // Retrieves address from received packet to send any information back
                    int port = receivedPacket.getPort();    // Retrieves port number of client from received packet
                    sendBuffer = "Received packet".getBytes();    // Turns return messgae into byte[] to send back to client

                    DatagramPacket sendingPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, port);    // Creates packet with message to be sent back to client
                    serverSocket.send(sendingPacket);

                }
            }

            if (filesReceived + i + 1 == sendAmount) {
                System.out.println("The average time to receive file " + fileName + " in millisecond is:  " + totalTime / sendAmount);
                System.out.println();
            }
        }

        serverSocket.close();   // Close socket after finishing
        System.out.println("I am done.");

    }
}

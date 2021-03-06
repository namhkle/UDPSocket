import java.net.*;    // InetAddress, DatagramSocket, DatagramPacket, UnknownHostException, SocketException
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.InterruptedException;
import java.lang.Thread;
import java.util.Arrays;

public class UDPClient {
    public static void main(String[] args) throws SocketException, UnknownHostException, IOException, InterruptedException {
        DatagramSocket clientSocket = new DatagramSocket();   // Creates a new Datagram socket and binds it to an open port on machine
        String fileName = "Test1 - large.txt";    // Holds the name of the file and poses as a way to easily change the name of the file

        String address = InetAddress.getLocalHost().toString().split("/")[1];   // Gets IPv4 address of machine acting as the server (same machine)
        Path filePath = Paths.get(fileName);    // Creates a Path object to be used when reading the bytes of the file

        int sendAmount = 100;
        long totalTime = 0;
        for (int i = 1; i <= sendAmount; i++) {
            byte[] fileBytes;   // Defines the byte[] that will hold the bytes of the file
            byte[] sendBuffer = new byte[1024];    // Creates a buffer for sending packet data the size of the file in bytes
            byte[] endBuffer = "File sent".getBytes();    // Message used to notify the server that the entire file has been sent
            byte[] receiveBuffer = new byte[1024];   // Creates a buffer for the receiving packet

            int filesSent = 0;    // Sets the number of files sent to 1 even though no files have been sent yet
            int port = 3537;    // Sets a known port number of the server. Can be changed if need be

            boolean fileSent = false;   // Pre-defined to false so while loop that runs until the file has been sent can run when it is reached
            DatagramPacket sendingPacket1;    // Defined the packet that is going to be used to send the file packets to the server
            fileBytes = Files.readAllBytes(filePath);   // Convert file to byte[] so it can be put into a Datagram Packet
            System.out.println("\nI am sending to server side: " + address);
            System.out.println("I am sending file " + fileName + " for the " + (filesSent + i) + "th time.");
            long startTime = System.nanoTime();   // Get starting time of process to send file to server to calculate the time it took later
            while (!fileSent) {

                if (fileBytes.length <= 1024) {    // If whats left to be sent is less than the buffer length (max for this program)

                    sendingPacket1 = new DatagramPacket(fileBytes, fileBytes.length, InetAddress.getByName(address), port);   // Creates packet with what was left to be sent to server
                    clientSocket.send(sendingPacket1);    // Send last part of file to server
                    fileSent = true;

                    DatagramPacket receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);    // Creates socket that will receive packets the length of buffer
                    clientSocket.receive(receivedPacket);   // Waits for a packet to be received from Client and stores it in receivedPacket when it arrives

                    long endTime = System.nanoTime();

                    System.out.println("I am finsihed sending file " + fileName + " for the " + (filesSent + i) + "th time.");
                    System.out.println("The time used in millisecond to send " + fileName + " for the " + (filesSent + i) + "th time is: " + ((endTime - startTime) / 1000000));

                    totalTime += (endTime - startTime) / 1000000;
                } else {

                    String receivedData = "Resend Packet";
                    byte[] subArray = Arrays.copyOfRange(fileBytes, 0, sendBuffer.length);    // Create a sub array of 60000 KB to send to server
                    fileBytes = Arrays.copyOfRange(fileBytes, sendBuffer.length, fileBytes.length);   // Shortens the byte[] of the files bytes to what has not been sent yet

                    sendingPacket1 = new DatagramPacket(subArray, subArray.length, InetAddress.getByName(address), port);   // Creates packet with 60000 KB of the file to be sent to server
                    clientSocket.send(sendingPacket1);    // Send part of file to server

                    DatagramPacket receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);    // Creates socket that will receive packets the length of buffer
                    clientSocket.receive(receivedPacket);   // Waits for a packet to be received from Client and stores it in receivedPacket when it arrives
                    receivedData = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

                }
            }

            if (filesSent + i == sendAmount) {
                System.out.println("The average time to send file " + fileName + " in millisecond is:  " + totalTime / sendAmount);
                System.out.println();
            }
        }

        clientSocket.close();   // Close socket after finishing
        System.out.println("I am done to send the file out.");

    }
}

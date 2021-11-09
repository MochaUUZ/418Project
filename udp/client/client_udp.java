import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This is the client class representing a client using the udp protocol
 */
public class client_udp {
    
    // These are the necessary variable the client will need
    private DatagramSocket datagramSocket;
    private InetAddress inetAddress;
    private int port;
    private byte[] buffer = new byte[1000];

    // Constructor
    public client_udp(DatagramSocket datagramSocket, InetAddress inetAddress, int portNumber)
    {
        this.datagramSocket = datagramSocket;
        this.inetAddress = inetAddress;
        this.port = portNumber;
    }

    /**
     * This is the method to listen and react to the server
     * @throws IOException This happen if something happen during the connection or command type format is incorrect
     */
    public void sendThenReceive() throws IOException
    {
        // Initilate all the variable value the client program will need
        Scanner scanner = new Scanner(System.in);
        int onOff = 1;
        String serverMessage = "";
        String message = "";

        // Loop that continue sent and listen for server message
        while(onOff == 1)
        {
            // Ready to take in user input
            buffer = new byte[300];
            serverMessage = "";
            System.out.println("Enter command: ");
            String answerC = scanner.nextLine();

            // Check for user command
            String[] command = answerC.split(" ");

            // Inform server about the command
            buffer = answerC.getBytes();
            DatagramPacket dataP = new DatagramPacket(buffer, buffer.length, inetAddress, port);
            datagramSocket.send(dataP);

            // Switch that have different operation based on the type of the command
            switch(command[0].toLowerCase())
            {
                case "put" :
                    // command for put
                    datagramSocket.receive(dataP);

                    // Read the file
                    File opener = new File(command[1]);
                    Scanner myReader = new Scanner(opener);
                    String data = "";
                    System.out.println("Awaiting server response...");

                    // Continue read in the file and store the data
                    while(myReader.hasNextLine())
                    {
                        data = data + myReader.nextLine() + "\n";
                    }

                    // Initiate a package containing the data info and sent to the server
                    message = "LEN:" + String.valueOf(data.getBytes().length);
                    dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                    datagramSocket.send(dataP);

                    // Receive a message from the server
                    datagramSocket.receive(dataP);

                    // Unpack the message from the server
                    serverMessage = new String(dataP.getData(), 0, dataP.getLength());

                    // If the server is readt to receive, then package the data and sent the data to server
                    if(serverMessage.equalsIgnoreCase("1"))
                    {
                        dataP = new DatagramPacket(data.getBytes(), data.getBytes().length, inetAddress, port);
                        datagramSocket.send(dataP);
                        int numbACK = data.getBytes().length / 1000;
                        for(int j = 0; j < numbACK; j++)
                        {
                            System.out.println("server: ACK");
                        }
                    }

                    // Listen to server
                    datagramSocket.receive(dataP);
                    serverMessage = new String(dataP.getData(), 0, dataP.getLength());
                    System.out.println("server: " + serverMessage);
                break;

                case "keyword" :
                // Inform user to wait for server message
                    System.out.println("Awaiting server response...");

                    // Reader to take in the package from the server
                    buffer = new byte[300];
                    dataP = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(dataP);
                    serverMessage = new String(dataP.getData(), 0, dataP.getLength());
                    System.out.println("server: " + serverMessage);
                break;

                case "get" : 
                    // Ready to receive a message from the server
                    buffer = new byte[300];
                    dataP = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(dataP);

                    // Unpack the server package
                    serverMessage = new String(dataP.getData(), 0, dataP.getLength());

                    // Construct a byte reader based on the information from the serve package
                    String[] getNum = serverMessage.split(":");
                    int byteSize = Integer.parseInt(getNum[1]);
                    buffer = new byte[byteSize];

                    // Send the message to the server
                    datagramSocket.send(dataP);

                    // Receive a message from the server
                    dataP = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(dataP);
                    serverMessage = new String(dataP.getData(), 0, dataP.getLength());

                    File file = new File(command[1]);

                    // Create the file and ready to unpack the server package onto the file
                    if(file.createNewFile())
                    {
                        FileWriter writer = new FileWriter(file);
                        writer.write(serverMessage);
                        writer.close();
                    }

                    // Inform the user that the fiel is successfully downloaded.
                    System.out.println("file " + command[1] + " downloaded");
                break;

                case "quit" :
                    onOff = 0;
                    datagramSocket.close();
                break;
            }
        }
        scanner.close();
    }

    /**
     * This is the main method of the java class which will initiate the program.
     * @param args This is the array containing all the user argument
     * @throws IOException This happen if something happen during the connection or the format is wrong
     */
    public static void main(String[] args) throws IOException{
        // The needed field variable for the client side
        String server_IP;
        int port;

        if(args.length == 2)
        {
            server_IP = args[0];
            port = Integer.valueOf(args[1]);
        }
        else
        {
            String errorMessage = "Error: Please use format- JavaFile <server_ip> <port>";
            throw new IOException(errorMessage);
        }

        // Run the java class with the data
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress host = InetAddress.getByName(server_IP);
        client_udp client = new client_udp(datagramSocket, host, port);
        System.out.println("Send datagram packets to a server!");
        client.sendThenReceive();
    }
}
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This is the java class for representing a server using the udp protocol 
 * Created by : Sheng hao Dong
 * Version : 1.0.0
 * 
 * ICSI 416 Fall 2021
 */
public class server_udp {
    // Private data field
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[256];

    // Constructor
    public server_udp(DatagramSocket datagramSocket)
    {
        this.datagramSocket = datagramSocket;
    }

    /**
     * Main method to listen and react to client
     * @throws IOException This happen if something happen during the connection or command type format is incorrect
     */
    public void receiveThenSend() throws IOException
    {
        // Local field that server will need
        int onOff = 1;
        String clientMessage = "";
        String message = "";

        // Loop that will continue listen and react to client message
        while(onOff == 1)
        {
            // Ready to receive data from client
            byte[] commandIntake = new byte[300];
            DatagramPacket dataP = new DatagramPacket(commandIntake, commandIntake.length);
            datagramSocket.receive(dataP);

            // Get the server ip and port of the received package
            InetAddress inetAddress = dataP.getAddress();
            int port = dataP.getPort();

            // Get the command type from the client
            String orig_str = new String(dataP.getData(), 0, dataP.getLength());
            String[] split_str = orig_str.split(" ");
            String command = split_str[0];

            // Switch that do different operation based on the different command
            switch(command.toLowerCase())
            {
                case "put" :
                // Sent message to client initiate server is ready to receive package
                    message = "1";
                    dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                    datagramSocket.send(dataP);

                    // get the byte size
                    dataP = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(dataP);
                    clientMessage = new String(dataP.getData(), 0, dataP.getLength());
                    String[] getNum = clientMessage.split(":");
                    int byteSize = Integer.parseInt(getNum[1]);
                    buffer = new byte[byteSize];

                    // Sent the data in the package
                    dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                    datagramSocket.send(dataP);

                    // Receive the package from the client
                    dataP = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(dataP);
                    clientMessage = new String(dataP.getData(), 0, dataP.getLength());

                    // Ready to unpack the package and import the data from the package received
                    File file = new File(split_str[1]);
                    if(file.createNewFile())
                    {
                        FileWriter writer = new FileWriter(file);
                        writer.write(clientMessage);
                        writer.close();
                    }

                    // Sent a success message to client indicating the file successfully uploaded.
                    message = "file uploaded.";
                    buffer = message.getBytes();
                    dataP = new DatagramPacket(buffer, buffer.length, inetAddress, port);
                    datagramSocket.send(dataP);
                break;

                case "keyword":
                // Error checking
                    if(split_str.length != 3)
                    {
                        message = "Error in command format. Ex) keyword XXXX XXX.txt";
                        dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                        datagramSocket.send(dataP);
                    }

                    // Get the command from the user
                    String[] splitOrigFile = split_str[2].split("\\.");
                    String newFilename = splitOrigFile[0] + "_anon.txt";

                    // Construct the anonymize text
                    String AnonymizeText = ""; 
                    for(int i = 0; i < split_str[1].length(); i++)
                    {
                        AnonymizeText = AnonymizeText + "X";
                    }

                    // Read the original file
                    System.out.println(split_str[2]);
                    File origFile = new File(split_str[2]);

                    // Error checking
                    if(!origFile.exists())
                    {
                        message = "Error: file doesn't exist.";
                        dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                        datagramSocket.send(dataP);
                    }

                    // Ready to scan the txt file
                    Scanner myReader = new Scanner(origFile);
                    String data = "";

                    // Ready to anonymize the file
                    File anonFile = new File(newFilename);

                    // Anonymizing the file if the file exist
                    if(anonFile.createNewFile())
                    {
                        FileWriter writer = new FileWriter(anonFile);

                        // Continue anoymizing the file and write the data onto the new file with the new filename
                        while(myReader.hasNextLine())
                        {
                            data = myReader.nextLine();
                            data = data.replaceAll(split_str[1], AnonymizeText);
                            writer.write(data);
                            writer.write("\n");
                        }
                        writer.close();

                        // Send the success message to the client
                        message = "File " + split_str[2] + " anonymized. Output file is " + newFilename;
                        dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                        datagramSocket.send(dataP);
                    }
                    else
                    {
                        // If the file with the new filename already exist
                        message = "Error: File already exist";
                        dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                        datagramSocket.send(dataP);
                    }
                break;

                case "get" : 
                    // Read the file
                    File myObj = new File(split_str[1]);
                    Scanner reader = new Scanner(myObj);
                    data = " ";

                    // Read the file if the file exist
                    if(myObj.exists())
                    {
                        while(reader.hasNextLine())
                        {
                            data = data + reader.nextLine() + "\n";
                        }
                        reader.close();
                    }

                    // Send the message containing package info to client
                    message = "LEN:" + String.valueOf(data.getBytes().length);
                    dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                    datagramSocket.send(dataP);

                    // Receive a package from the client
                    datagramSocket.receive(dataP);

                    // Report the client message
                    clientMessage = new String(dataP.getData(), 0, dataP.getLength());
                    dataP = new DatagramPacket(data.getBytes(), data.getBytes().length, inetAddress, port);
                    datagramSocket.send(dataP);
                    int numbACK = data.getBytes().length / 1000;
                    for(int i = 0; i < numbACK; i++)
                    {
                        System.out.println("client: ACK");
                    }
                break;

                case "quit" : 
                    onOff = 0;
                    datagramSocket.close();
                break;

                default : 
                    message = "Command not recognized. Please enter again";
                    buffer = message.getBytes();
                    dataP = new DatagramPacket(buffer, buffer.length, inetAddress, port);
                    datagramSocket.send(dataP);
                break;
            }
        }
    }

    /**
     * This is the main method of the java class which will initiate the program.
     * @param args This is the array containing all the user argument
     * @throws IOException This happen if something happen during the connection or the format is wrong
     */
    public static void main(String[] args) throws IOException {
        // The needed field variable for the server side.
        String server_IP;
        int port;

        // Error checking
        if(args.length == 1)
        {
            port = Integer.valueOf(args[0]);
        }
        else
        {
            String errorMessage = "Error: Please use format- JavaFile <port>";
            throw new IOException(errorMessage);
        }

        // Try to get the server's ip address for the client to connect
        InetAddress host = InetAddress.getLocalHost();
        server_IP = host.getHostAddress();
        System.out.println("InetAddress : " + server_IP);

        // Run the server with the data
        DatagramSocket datagramSocket = new DatagramSocket(port);
        server_udp server = new server_udp(datagramSocket);
        server.receiveThenSend();
    }   
}
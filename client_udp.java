import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class client_udp {
    
    private DatagramSocket datagramSocket;
    private InetAddress inetAddress;
    private byte[] buffer = new byte[1000];

    public client_udp(DatagramSocket datagramSocket, InetAddress inetAddress)
    {
        this.datagramSocket = datagramSocket;
        this.inetAddress = inetAddress;
    }

    public void sendThenReceive() throws IOException
    {
        Scanner scanner = new Scanner(System.in);
        int onOff = 1;

        String serverMessage = "";
        String message = "";

        while(onOff == 1)
        {
            buffer = new byte[300];
            serverMessage = "";
            System.out.println("Enter command: ");
            String answerC = scanner.nextLine();

            // Check for user command
            String[] command = answerC.split(" ");

            // Inform server about the command
            buffer = answerC.getBytes();
            DatagramPacket dataP = new DatagramPacket(buffer, buffer.length, inetAddress, 12250);
            datagramSocket.send(dataP);

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

                    // while(myReader.hasNextLine())
                    for(int i = 0; i < 5; i++)
                    {
                        data = data + myReader.nextLine() + "\n";
                    }

                    message = "LEN:" + String.valueOf(data.getBytes().length);
                    dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, 12250);
                    datagramSocket.send(dataP);

                    datagramSocket.receive(dataP);

                    serverMessage = new String(dataP.getData(), 0, dataP.getLength());
                    // Send the package to the server
                    if(serverMessage.equalsIgnoreCase("1"))
                    {
                        dataP = new DatagramPacket(data.getBytes(), data.getBytes().length, inetAddress, 12250);
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
                    System.out.println("Awaiting server response...");
                    buffer = new byte[300];
                    dataP = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(dataP);
                    serverMessage = new String(dataP.getData(), 0, dataP.getLength());
                    System.out.println("server: " + serverMessage);
                break;

                case "get" : 
                    // code for get
                    buffer = new byte[300];
                    dataP = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(dataP);
                    serverMessage = new String(dataP.getData(), 0, dataP.getLength());
                    String[] getNum = serverMessage.split(":");
                    int byteSize = Integer.parseInt(getNum[1]);
                    buffer = new byte[byteSize];

                    datagramSocket.send(dataP);

                    dataP = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(dataP);
                    serverMessage = new String(dataP.getData(), 0, dataP.getLength());

                    File file = new File(command[1]);
                    if(file.createNewFile())
                    {
                        FileWriter writer = new FileWriter(file);
                        writer.write(serverMessage);
                        writer.close();
                    }

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

    public static void main(String[] args) throws IOException{
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress inetAddress = InetAddress.getByName("localhost");
        client_udp client = new client_udp(datagramSocket, inetAddress);
        System.out.println("Send datagram packets to a server!");
        client.sendThenReceive();
    }
}
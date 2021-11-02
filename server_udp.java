import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class server_udp {
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[256];

    public server_udp(DatagramSocket datagramSocket)
    {
        this.datagramSocket = datagramSocket;
    }

    public void receiveThenSend() throws IOException
    {
        int onOff = 1;
        String clientMessage = "";
        String message = "";

        while(onOff == 1)
        {
            byte[] commandIntake = new byte[300];
            DatagramPacket dataP = new DatagramPacket(commandIntake, commandIntake.length);
            datagramSocket.receive(dataP);
            InetAddress inetAddress = dataP.getAddress();

            int port = dataP.getPort();

            String orig_str = new String(dataP.getData(), 0, dataP.getLength());
            String[] split_str = orig_str.split(" ");
            String command = split_str[0];
            System.out.println(buffer.length);

            switch(command.toLowerCase())
            {
                case "put" :
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

                    dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                    datagramSocket.send(dataP);

                    dataP = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(dataP);
                    clientMessage = new String(dataP.getData(), 0, dataP.getLength());

                    File file = new File(split_str[1]);
                    if(file.createNewFile())
                    {
                        FileWriter writer = new FileWriter(file);
                        writer.write(clientMessage);
                        writer.close();
                    }

                    message = "file uploaded.";
                    buffer = message.getBytes();
                    dataP = new DatagramPacket(buffer, buffer.length, inetAddress, port);
                    datagramSocket.send(dataP);
                break;

                case "keyword":
                    if(split_str.length != 3)
                    {
                        message = "Error in command format. Ex) keyword XXXX XXX.txt";
                        dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                        datagramSocket.send(dataP);
                    }

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

                    Scanner myReader = new Scanner(origFile);
                    String data = "";

                    // Ready to anonymize the file
                    File anonFile = new File(newFilename);

                    if(anonFile.createNewFile())
                    {
                        FileWriter writer = new FileWriter(anonFile);

                        while(myReader.hasNextLine())
                        {
                            data = myReader.nextLine();
                            data = data.replaceAll(split_str[1], AnonymizeText);
                            writer.write(data);
                            writer.write("\n");
                        }
                        writer.close();
                        message = "File " + split_str[2] + " anonymized. Output file is " + newFilename;
                        dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                        datagramSocket.send(dataP);
                    }
                    else
                    {
                        message = "Error: File already exist";
                        dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                        datagramSocket.send(dataP);
                    }
                break;

                case "get" : 
                    // command gor get
                    //datagramSocket.receive(dataP);

                    // Read the file
                    File myObj = new File(split_str[1]);
                    Scanner reader = new Scanner(myObj);
                    data = " ";

                    if(myObj.exists())
                    {
                        // while(reader.hasNextLine())
                        for(int i = 0; i < 5; i++)
                        {
                            data = data + reader.nextLine() + "\n";
                        }
                        reader.close();
                    }

                    message = "LEN:" + String.valueOf(data.getBytes().length);
                    dataP = new DatagramPacket(message.getBytes(), message.getBytes().length, inetAddress, port);
                    datagramSocket.send(dataP);

                    datagramSocket.receive(dataP);

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

    public static void main(String[] args) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket(12250);
        server_udp server = new server_udp(datagramSocket);
        server.receiveThenSend();
    }
    
}

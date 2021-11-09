import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class server_tcp {
    public static void main(String[] args) throws IOException {
        
        // The needed field variable for the server side.
        String server_IP;
        int port;

        if(args.length == 1)
        {
            port = Integer.valueOf(args[0]);
        }
        else
        {
            String errorMessage = "Error: Please use format- JavaFile <server_ip> <port>";
            throw new IOException(errorMessage);
        }

        // Try to get the server's ip address for the client to connect
        InetAddress host = InetAddress.getLocalHost();
        server_IP = host.getHostAddress();
        System.out.println("InetAddress : " + server_IP);

        // Get the server socket running. The port need to be 12000 ~ 13000
        ServerSocket ss = new ServerSocket(port);
        Socket s = ss.accept();
        System.out.println("client connected");

        // for talking to client
        PrintWriter pr = new PrintWriter(s.getOutputStream());

        // For listening to client
        InputStreamReader in = new InputStreamReader(s.getInputStream());
        BufferedReader bf = new BufferedReader(in);

        // for holding client's file
        String clientMessage = "";

        int onOff = 1;

        // Loop for continue listening to client and give reaction until terminate message is listened.
        while(onOff == 1)
        {
            // Getting which command the user entered
            String orig_str = bf.readLine();
            String[] command_St = orig_str.split(" ");
            System.out.println("\nclient : " + command_St[0]);
            String command = command_St[0];

            // A swtich that based on different command, different operation is taken
            switch(command.toLowerCase())
            {
                case "put" : 
                    // Error check
                    if(command_St.length != 2)
                    {
                        pr.println("Error in command format. Ex: put XXX.txt");
                        pr.flush();
                        break;
                    }

                    // tell client ready to receive file.
                    pr.println("1");
                    pr.flush();

                    File file = new File(command_St[1]);

                    // Receiving the file data
                    if(file.createNewFile())
                    {
                        FileWriter writer = new FileWriter(file);
                        while(!(clientMessage = bf.readLine()).isEmpty())
                        {
                            writer.write(clientMessage);
                            writer.write("\n");
                        }
                        writer.close();
                        pr.println("File uploaded");
                        pr.flush();
                    }
                    else
                    {
                        pr.println("Fail to upload. File already exist.");
                        pr.flush();
                    }
                break;

                case "keyword" :
                // error checking
                    if(command_St.length != 3)
                    {
                        pr.println("Error in command format. Ex) keyword XXXX XXX.txt");
                        pr.flush();
                        break;
                    }

                    // Ready to create the new file for holding anonymized data.
                    String[] splitOrigFile = command_St[2].split("\\.");
                    String newFileName = splitOrigFile[0] + "_anon.txt";
                    
                    // Construct the anonymize text
                    String AnonymizeText = "";
                    for(int i = 0; i < command_St[1].length(); i++)
                    {
                        AnonymizeText = AnonymizeText + "X";
                    }

                    // Read the orignal file
                    File origFile = new File(command_St[2]);

                    // Error checking
                    if(!origFile.exists())
                    {
                        pr.println(command_St[2] + " Error: file doesn't exist. ");
                        pr.flush();
                        break;
                    }
                    
                    Scanner myReader = new Scanner(origFile);
                    String data = "";
                    
                    // Ready to anonymize the file
                    File AnonFile = new File(newFileName);

                    // Creating the file if current dir doesn't have the filename.
                    if(AnonFile.createNewFile())
                    {
                        FileWriter writer = new FileWriter(AnonFile);

                        // Continue writing in data onto the file
                        while(myReader.hasNextLine())
                        {
                            data = myReader.nextLine();
                            data = data.replaceAll(command_St[1], AnonymizeText);
                            writer.write(data);
                            writer.write("\n");
                        }
                        // Send the completed file information to the client
                        writer.close();
                        pr.println("File " + command_St[2] + " anonymized. Output file is " + newFileName);
                        pr.flush();
                    }
                    else
                    {
                        // Error checking, if there is already exist the file with same filename.
                        pr.println("Error: Already anonymized.");
                        pr.flush();
                        break;
                    }

                break;

                case "get" :
                // Creating a file for reading
                    File myObj = new File(command_St[1]);
                    Scanner reader = new Scanner(myObj);
                    data = "";

                    // Ready to exporting the data if the current dir exist such file with the filename
                    if(myObj.exists())
                    {
                        while(reader.hasNextLine())
                        {
                            data = data + reader.nextLine() + "\n";
                        }
                        pr.println(data);
                        pr.flush();
                    }
                break;

                case "quit" : 
                    pr.print("Exiting...");
                    onOff = 0;
                    pr.flush();
                    ss.close();
                break;

                default : 
                pr.println("Command not recognized. Please enter again");
                pr.flush();
            }
        }
    }
}
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class client_tcp {
    public static void main(String[] args) throws UnknownHostException, IOException {
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
        
        // Connecting to the server
        Socket s = new Socket(server_IP, port);
        Scanner userInput = new Scanner(System.in);
        int onOff = 1;

        // for talking to server
        PrintWriter pr = new PrintWriter(s.getOutputStream());

        // For listening to server
        InputStreamReader in = new InputStreamReader(s.getInputStream());
        BufferedReader bf = new BufferedReader(in);
        String serverMessage = "";

        // Loop for continue sent message to server and listen for server's reaction
        while(onOff == 1)
        {
            serverMessage = "";
            System.out.print("Enter command: ");
            String answerC = userInput.nextLine(); 

            // check for user command
            String[] command = answerC.split(" ");

            // Inform server the command
            pr.println(answerC);
            pr.flush();

            // A switch that based on different command received, different operation is taken
            switch(command[0].toLowerCase())
            {
                case "put" : 
                    // command for put
                    serverMessage = bf.readLine();

                    // Read the file
                    File myObj = new File(command[1]);
                    Scanner myReader = new Scanner(myObj);
                    String data = "";
                    
                    // If the server is ready to receive data
                    if(serverMessage.equalsIgnoreCase("1"))
                    {
                        System.out.println("Awaiting server response...");
                        while(myReader.hasNextLine())
                        {
                            data = data + myReader.nextLine() + "\n";
                        }
                        pr.println(data);
                        pr.flush();

                        // Listen to server
                        serverMessage = bf.readLine();
                        System.out.println("server : " + serverMessage);
                    }
                    else 
                    {
                        // This happen when the server is currently not ready to receive data
                        System.out.println("server : " + serverMessage);
                    }
                    
                break;

                case "keyword" : 
                    // command for keyword
                    System.out.println("Awaiting server response...");
                    serverMessage = bf.readLine();
                    System.out.println("server : " + serverMessage);
                break;

                case "get" :
                    // error checking
                    if(command.length != 2)
                    {
                        System.out.println("Error in command format. Ex: get XXX.txt");
                        break;
                    }

                    File file = new File(command[1]);

                    // Receiving the file data
                    if(file.createNewFile())
                    {
                        FileWriter writer = new FileWriter(file);
                        while(!(serverMessage = bf.readLine()).isEmpty())
                        {
                            writer.write(serverMessage);
                            writer.write("\n");
                        }
                        writer.close();
                        System.out.println("File " + command[1] + " downloaded.");
                    }
                    else
                    {
                        // This happen when there exist a file with the filename
                        System.out.println("Fail to download. File already exist.");
                        break;
                    }
                break;

                case "quit" : 
                    onOff = 0;
                    s.close();
                    System.out.println("Exiting program!");
                break;

                default : 
                System.out.println("Command not recognized. Please enter again");
            }
        }
        userInput.close();
    }
}
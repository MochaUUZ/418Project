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
        Socket s = new Socket("localhost", 12250);
        Scanner userInput = new Scanner(System.in);
        int onOff = 1;

        // for talking to server
        PrintWriter pr = new PrintWriter(s.getOutputStream());

        // For listening to server
        InputStreamReader in = new InputStreamReader(s.getInputStream());
        BufferedReader bf = new BufferedReader(in);
        String serverMessage = "";

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

            switch(command[0].toLowerCase())
            {
                case "put" : 
                    // command for put
                    serverMessage = bf.readLine();

                    // Read the file
                    File myObj = new File(command[1]);
                    Scanner myReader = new Scanner(myObj);
                    String data = "";
                    
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
                    // command for get
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
                        System.out.println("Fail to download. File already exist.");
                        break;
                    }
                break;

                case "quit" : 
                    onOff = 0;
                    System.out.println("Exiting program!");
                break;

                default : 
                System.out.println("Command not recognized. Please enter again");
            }
        }
        userInput.close();
        s.close();
    }
}
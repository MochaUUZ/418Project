import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class server_tcp {
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(12250);
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
        while(onOff == 1)
        {
            String orig_str = bf.readLine();
            String[] str = orig_str.split(" ");
            System.out.println("\nclient : " + str[0]);
            String command = str[0];

            switch(command)
            {
                case "put" : 
                    // Error check
                    if(str.length != 2)
                    {
                        pr.println("Error in command format. Ex: put XXX.txt");
                        pr.flush();
                        break;
                    }

                    // tell client ready to receive file.
                    pr.println("1");
                    pr.flush();

                    File file = new File(str[1]);

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
                    if(str.length != 3)
                    {
                        pr.println("Error in command format. Ex) keyword XXXX XXX.txt");
                        pr.flush();
                        break;
                    }

                    String[] splitOrigFile = str[2].split("\\.");
                    String newFileName = splitOrigFile[0] + "_anon.txt";

                    // Construct the anonymize text
                    String AnonymizeText = "";
                    for(int i = 0; i < str[1].length(); i++)
                    {
                        AnonymizeText = AnonymizeText + "X";
                    }

                    // Read the orignal file
                    File origFile = new File(str[2]);

                    // Error checking
                    if(!origFile.exists())
                    {
                        pr.println(str[2] + " Error: file doesn't exist. ");
                        pr.flush();
                        break;
                    }
                    
                    Scanner myReader = new Scanner(origFile);
                    String data = "";
                    
                    // Ready to anonymize the file
                    File AnonFile = new File(newFileName);

                    if(AnonFile.createNewFile())
                    {
                        FileWriter writer = new FileWriter(AnonFile);

                        while(myReader.hasNextLine())
                        {
                            data = myReader.nextLine();
                            data = data.replaceAll(str[1], AnonymizeText);
                            writer.write(data);
                            writer.write("\n");
                        }
                        writer.close();
                        pr.println("File " + str[2] + " anonymized. Output file is " + newFileName);
                        pr.flush();
                    }
                    else
                    {
                        pr.println("Error: Already anonymized.");
                        pr.flush();
                        break;
                    }

                break;

                case "get" :
                    File myObj = new File(str[1]);
                    Scanner reader = new Scanner(myObj);
                    data = "";

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
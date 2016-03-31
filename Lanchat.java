package lanchat;

import java.util.Scanner;

public class Lanchat
{
    public static void main(String [] args)
	{  
        String username;
        
        if (args.length == 0)
        {
            username = ChatWindow.usernamePrompt();
            new Chatroom(username, Config.DEFAULT_STATUS, true);
        }
        else
        {
            System.out.println("Lanchat\n\nType /h for help on commands\n\n");
            
            username = args[0];
            
            Chatroom chat = new Chatroom(username, Config.DEFAULT_STATUS, false);
    
            String input;
            Scanner scan = new Scanner(System.in);
        
            while (true)
            {
                System.out.print(username+"> ");
                input = scan.nextLine();
                
                if (input.length() == 0) continue;
                
                String [] inputs = input.split("\\s+");
                
                if ("/".equals(inputs[0].substring(0,1)) && input.length() > 1)
                {
                    switch (inputs[0].substring(1,2))
                    {
                        case "h":   System.out.println("\n"
                                    + "h                               Show help\n"
                                    + "s [o]nline|[i]nvisible|[a]way   Change status\n"
                                    + "l                               List users online\n"
                                    + "p [username] [message]          Send private message to user\n"
                                    + "q                               Quit Lanchat"
                                    );
                                    break;

                        case "s":	
                                switch (inputs[1])
                                {
                                    case "o":   chat.setStatus(Status.ONLINE); break;
                                    case "i":   chat.setStatus(Status.INVISIBLE); break;
                                    case "a":   chat.setStatus(Status.AWAY); break;
                                    default:    System.out.println("invalid status option");
                                }
                                break;
                                    
                        case "l":	chat.printPeers(); break;
                        //case "p":	
                        case "q":   System.exit(0);
                        default:    System.out.println("invalid command");
                    }
                    System.out.println();
                }
                else
                {
                    chat.sendMessage(input);
                }
            }
        }
    }
}

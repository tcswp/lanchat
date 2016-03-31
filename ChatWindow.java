package lanchat;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

public class ChatWindow extends JFrame
{
    JPanel rightPane = new JPanel();
    JPanel leftPane = new JPanel();
    JPanel lowerPane = new JPanel();
    JButton sendButton = new JButton("Send");
    private static JTextField inputArea = new JTextField(25);
    private static JTextArea convArea = new JTextArea(20,25);
    private static JTextArea buddyList = new JTextArea(21,10);
    
    private static String username;

    ChatWindow(String username)
    {
        setTitle("Lanchat");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Container c = getContentPane();
        
        rightPane.setLayout(new BorderLayout(5,5));
        lowerPane.setLayout(new BorderLayout(5,5));
        lowerPane.add(inputArea, BorderLayout.CENTER);
        lowerPane.add(sendButton, BorderLayout.EAST);
        
        sendButton.addActionListener(new sendMessageListener());
        inputArea.addKeyListener(new sendMessageListener());
        
        rightPane.add(lowerPane, BorderLayout.SOUTH);
        convArea.setEditable(false);
        rightPane.add(new JScrollPane(convArea), BorderLayout.CENTER);
        
        leftPane.setLayout(new BorderLayout());
        buddyList.setEditable(false);
        leftPane.add(new JScrollPane(buddyList), BorderLayout.CENTER);
        
        this.username = username; // for sendMessage()
        buddyList.append("~"+username+"\n");
        
        c.setLayout(new BorderLayout(5,5));
        c.add(rightPane, BorderLayout.CENTER);
        c.add(leftPane, BorderLayout.WEST);
        
        pack();
        setVisible(true);
    }
    
    public void updateBuddyList(ArrayList<User> peers)
    {
        buddyList.setText(null);
        buddyList.append("~"+username+"\n");
        for (User peer : peers)
            buddyList.append(peer.getUsername()+"\n");
    }
    
    public void printMessage(String username, String message)
    {
        convArea.append(message + "\n");
    }
    
    public static String usernamePrompt()
    {
        JFrame frame = new JFrame("Username Prompt");
        String username = JOptionPane.showInputDialog(frame, "enter your username below");
        return username;
    }
    
    private static void sendGuiMessage()
    {
        String text = inputArea.getText();
        if (text == null)
            return;
        convArea.append(username+": "+text + "\n");
        inputArea.setText(null);
        Chatroom.sendMessage(text);
    }
    
    class sendMessageListener implements ActionListener, KeyListener
    {
        public void actionPerformed(ActionEvent event)
        {
            sendGuiMessage();
        }
        
        @Override
        public void keyPressed(KeyEvent e)
        {
            if (e.getKeyCode()==KeyEvent.VK_ENTER)
                sendGuiMessage();
        }
        @Override
        public void keyReleased(KeyEvent e) {}

        @Override
        public void keyTyped(KeyEvent e) {}
    }
}	

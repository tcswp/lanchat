package lanchat;

import java.net.*;
import java.io.*;

import java.util.*;

public class Chatroom
{
	private static ArrayList<User> peers;
	private static DatagramSocket socket;
	private static User me;
	private static InetAddress broadcastAddr;
	private static boolean isGraphical;
	private static ChatWindow window;
	
	private InetAddress getBroadcastAddr(InetAddress ip)
	{
        InetAddress broadcast = null;
        try
        {
            NetworkInterface iface = NetworkInterface.getByInetAddress(ip);
            List<InterfaceAddress> addrs = iface.getInterfaceAddresses();
            
            for (InterfaceAddress addr : addrs)
                if (addr.getBroadcast() != null)
                    broadcast = addr.getBroadcast();
        }
        catch (SocketException e) { e.printStackTrace(); }
        
        return broadcast;
	}
	
	public Chatroom(String username, Status status, boolean useGui)
	{
        if (useGui)
        {
            isGraphical = true;
            window = new ChatWindow(username);
        }
        
        try
        {
            me = new User(username, status, InetAddress.getLocalHost());
            socket = new DatagramSocket(Config.LISTEN_PORT);
            socket.setBroadcast(true);
            broadcastAddr = getBroadcastAddr(me.getIp());
            signOn();
            peers = new ArrayList<User>();
            Thread listenThread = new Thread(new ListenThread());
            listenThread.start();
            Thread shutdownHook = new Thread(new ShutdownHook());
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
        catch (UnknownHostException e) { e.printStackTrace(); }
        catch (SocketException e) { e.printStackTrace(); }
	}
	
	public void printPeers()
	{
    System.out.println("\n"+me.getUsername()+" ("+me.getStatus()+")");
    if (peers != null)
    {
      for (User peer : peers)
          System.out.println(peer.getUsername()+"@"+peer.getIp().getHostAddress()+" ("+peer.getStatus()+")");
    }
	}
	
	private static User getPeer(InetAddress ip)
	{
    User u = null;
		for (User peer : peers)
			if (peer.getIp().equals(ip))
			{
        u = peer;
        break;
      }
		return u;
	}
	
	private static void signOn()
	{
    byte[] hello = ("hi "+me.getUsername()+" "+me.getStatus()).getBytes();
    try
    {
      DatagramPacket packet = new DatagramPacket(hello, hello.length, broadcastAddr, Config.LISTEN_PORT);
      socket.send(packet);
    }
    catch (Exception e) { e.printStackTrace(); }
	}
	
	private static void signOff()
	{
    byte [] bye = "bye ".getBytes();
    try
    {
      DatagramPacket packet = new DatagramPacket(bye, bye.length, broadcastAddr, Config.LISTEN_PORT);
      socket.send(packet);
    }
    catch (Exception e) { e.printStackTrace(); }
  }
    
  private static void acknowledge(InetAddress ip)
  {
      byte[] hello = ("sup "+me.getUsername()+" "+me.getStatus()).getBytes();
      try
      {
          DatagramPacket packet = new DatagramPacket(hello, hello.length, ip, Config.LISTEN_PORT);
          socket.send(packet);
      }
      catch (Exception e) { e.printStackTrace(); }
  }

	public void setStatus(Status status)
	{
    if (status == me.getStatus())
      return;
	
    Status oldStatus = me.getStatus();
    me.setStatus(status);
	
    if (status == Status.INVISIBLE)
    {
      signOff();
      return;
    }
    
    if (status == Status.ONLINE && oldStatus == Status.INVISIBLE)
    {
      signOn();
      return;
    }
    
    byte[] update = ("update "+status).getBytes();
    try
    {
      for (User peer : peers)
      {
        DatagramPacket packet = new DatagramPacket(update, update.length, peer.getIp(), Config.LISTEN_PORT);
        socket.send(packet);
      }
    }
    catch (Exception e) { e.printStackTrace(); }
  }
	
	public static void sendMessage(String message)
	{
		byte [] msg = ("msg "+message).getBytes();
		for (User peer : peers)
		{
			try
			{
				DatagramPacket packet = new DatagramPacket(msg, msg.length, peer.getIp(), Config.LISTEN_PORT);
				socket.send(packet);
			}
			catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	// public void sendPrivateMessage(User recipient, String message)
	// {
		// check if recipient exists and if so  send message otherwise return error
	// }
	
	private static class ShutdownHook implements Runnable { public void run() { signOff(); } }
	
	private static class ListenThread implements Runnable
	{		
		public void run()
		{			
			while (true)
			{
				byte [] recvBuffer = new byte[100];
				try
				{
          DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);
          socket.receive(packet);

          InetAddress addr = packet.getAddress();
          if (addr.equals(me.getIp()))
              continue;
          
          byte [] data = packet.getData();
          String msg = new String(data);
          String[] fields = msg.split(" ",2);
          String messageType = fields[0];
          String body = ""; 
          if (fields.length > 1)
            body = fields[1];
          String[] dataFields = body.split(" ", 2);
                    
          String username;
          Status status;
          User peer;
          
          String notification;
                              
          switch (messageType)
          {
            case "hi":
                    username = dataFields[0];
                    status = Status.valueOf(dataFields[1].trim());
                    
                    peer = new User(username,status,addr);
                    peers.add(peer);
                    acknowledge(addr);
        
                    notification = "*** "+username+"@"+addr.getHostAddress()+" has just signed on ("+status+")";
                    if (isGraphical)
                    {
                        window.printMessage(username, notification);
                        window.updateBuddyList(peers);
                    }
                    else System.out.println(notification);
                    
                    break;
                    
            case "sup":
                    username = dataFields[0];
                    status = Status.valueOf(dataFields[1].trim());
                    
                    peer = new User(username,status,addr);
                    peers.add(peer);
                    
                    if (isGraphical) window.updateBuddyList(peers);
                    continue;
                    
            case "update":
                    status = Status.valueOf(dataFields[0].trim());
                    peer = getPeer(addr);
                    peer.setStatus(status);
                    username = peer.getUsername();
                    
                    notification = "*** "+username+" changed their status to "+status;
                    if (isGraphical) window.printMessage(username, notification); else System.out.println(notification);
                    break;
            
            case "msg":
                    peer = getPeer(addr);
                    username = peer.getUsername();
                
                    notification = username+": "+body;
                    if (isGraphical) window.printMessage(username, notification); else System.out.println("\r"+notification);
                    break;
                    
            case "bye":
                    peer = getPeer(addr);
                    username = peer.getUsername();
                    peers.remove(peer);
                    notification = "*** "+username+" signed off";
                    if (isGraphical)
                    {
                        window.printMessage(username, notification);
                        window.updateBuddyList(peers);
                    }
                    else System.out.println(notification);
                    break;

            default: System.out.println("got unrecognized packet: \""+msg+"\"");
          }
          if (!isGraphical) System.out.print("\r"+me.getUsername()+"> ");
        }
				catch (IOException e) { e.printStackTrace(); System.exit(0); }
			}
		}
	}
}

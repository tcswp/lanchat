package lanchat;

import java.net.InetAddress;

public class User
{
	private String username;
	private Status status;
	private InetAddress ip;

	public User() {}

	public User(String username, Status startStatus, InetAddress ip)
	{
		this.username = username;
		this.ip = ip;
		status = startStatus;
	}
	
//     @Override
//     public boolean equals(Object obj)
//     {
//         User user = (User) obj;
//         if (obj == null)
//             return false;
//             
//         if (this.username == user.username
//             && this.status == user.status
//             && this.ip == user.ip)
//             return true;
//         return false;
// 	}
	
	public void setStatus(Status newStatus){ status = newStatus; }
	
	public String getUsername()	{ return username; }
	public Status getStatus()	{ return status; }
	public InetAddress getIp()	{ return ip; }
}

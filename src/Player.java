import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Player implements Comparable<Player> {
	public String name;
	public Socket client;
	public boolean alive;
	protected boolean disabled;
	public boolean inCult;
	public boolean isMafia;
	public String className;
	protected int ordering;
	protected CultLeader cultLeader;
	private PrintWriter out;
	private BufferedReader in;
	
	public Player() {
		alive = true;
		disabled = false;
		inCult = false;
		isMafia = false;
		cultLeader = null;
	}
	
	public void receiveCultPowers(CultLeader cult) {
		this.cultLeader = cult;
		try {
			receiveMessage("The cult leader has died. Long live the new cult leader! Each night, be careful of whether you are" +
			" acting as cult leader or taking a normal action. You can see which in the title of the window that opens.");
		} catch (IOException e) {
		}
	}
	
	public String getNightAction(ArrayList<String> names) throws IOException {
		disabled = false;
		if (out == null)
			out = new PrintWriter(client.getOutputStream(), true);
		out.println(className + "Players: " + String.join(",", names));
		if (in == null)
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		String s = in.readLine();
		if (this instanceof Axe) {
			out.println(className  + " (axe part)Players: " + String.join(",", names));
			s = in.readLine() + "," + s;
		}
		if (cultLeader != null && !(this instanceof CultLeader)) {
			out.println("Cult Leader"  + "Players: " + String.join(",", names));
			String temp = className;
			className = cultLeader.className;
			s += "," + in.readLine() + (extraNightAction("Do you want to disband?") ? "t" : "f");
			className = temp;
		}
		return s;
	}
	
	public int compareTo(Player oth) {
		return ordering < oth.ordering ? -1 : ordering == oth.ordering ? 0 : 1;
	}
	
	public boolean extraNightAction(String message) throws IOException {
		if (out == null)
			out = new PrintWriter(client.getOutputStream(), true);
		out.println(message);
		out.println(className + "Yes or no?");
		if (in == null)
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		return in.readLine().equals("Yes");
	}
	
	public abstract void takeEffect(Player p, boolean action);
	
	public void receiveMessage(String message) throws IOException {
		if (out == null)
			out = new PrintWriter(client.getOutputStream(), true);
		out.println(message);
	}
	
	public void initialConnect() throws IOException {
		if (out == null)
			out = new PrintWriter(client.getOutputStream(), true);
		if (in == null)
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		name = "";
		while (name.length() == 0) {
			out.println("What is your name?");
			name = in.readLine();
			for (char c: name.toCharArray())
				if (!Character.isAlphabetic(c)) {
					out.println("Invalid name.");
					name = "";
					break;
				}
		}
	}
	
	public abstract void onDeath(HashMap<String, Player> players);
}

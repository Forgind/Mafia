import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
	
	private static Client.ButtonPanel panel;

	public static void main(String[] args) throws IOException, InterruptedException {
		ServerSocket server = new ServerSocket(12365);
		int numPlayers = 5;
		//Player[] players = { new Alien(), new Axe(), new Bomb(), new Mafia(), new Prostitute(), new CultLeader(),
		//		new Cassanova(), new Cop(), new Doctor(), new GrandInquisitor(), new Hunter(), new Town(), new Traveler(), new Town() };
		Random r = new Random();
		//Player[] players = { new Town(), new Doctor(), new Cop(), new Mafia(), new Traveler() };
		//Player[] players = { new Alien(), new Axe(), new Bomb(), new Prostitute(), new Hunter() };
		//Player[] players = { new Cassanova(), new Town(), new Mafia() };
		Player[] players = { new Prostitute(), new CultLeader(), new GrandInquisitor(), new Mafia(), new Doctor() };
		String[] lovers = new String[2];
		String proxy = "";
		HashMap<String, Player> playersByName = new HashMap<String, Player>();
		boolean onlyProstitute = false;
		while (numPlayers > 0) {
			Socket clientSocket = server.accept();
			int ind = r.nextInt(numPlayers);
			numPlayers--;
			Player temp = players[ind];
			temp.client = clientSocket;
			temp.initialConnect();
			System.out.println(temp.name + " is " + temp.className);
			temp.receiveMessage("You are the " + temp.className);
			playersByName.put(temp.name, temp);
			players[ind] = players[numPlayers];
			players[numPlayers] = temp;
		}
		
		ArrayList<String> names = new ArrayList<String>();
		for (String p: playersByName.keySet()) {
			names.add(p);
		}
		
		for (String s: playersByName.keySet()) {
			Player p = playersByName.get(s);
			if (p instanceof Cassanova) {
				lovers[0] = p.name;
				p.receiveMessage("Please select your lover.");
				lovers[1] = p.getNightAction(names);
				int choice = p.extraNightAction("Your two choices are " + players[0].className + " or "
				+ players[1].className + ". Select \"Yes\" to choose the first option.") ? 0 : 1;
				if (!players[choice].isMafia && players[1 - choice].isMafia) {
					p.receiveMessage("Your choice was rejected. You must select the mafia.");
					choice = 1 - choice;
				}
				players[choice].client = p.client;
				players[choice].name = p.name;
				playersByName.put(p.name, players[choice]);
			}
			else if (p instanceof Alien) {
				names.remove(p.name);
				p.receiveMessage("Please select your proxy.");
				proxy = p.getNightAction(names);
				names.add(p.name);
			}
		}
		
		ArrayList<String> mafiaMembers = new ArrayList<String>();
		for (Player p: playersByName.values()) {
			if (p.isMafia && !(p instanceof Prostitute))
				mafiaMembers.add(p.name);
		}
		for (Player p: playersByName.values()) {
			if (p.isMafia && !(p instanceof Prostitute)) {
				p.receiveMessage("Your fellow mafia members are:");
				for (String n: mafiaMembers)
					if (!p.name.equals(n))
						p.receiveMessage(n);
			}
			if (p.name.equals(lovers[0]))
				p.receiveMessage("You are in love with " + lovers[1] + ".");
			if (p.name.equals(lovers[1]))
				p.receiveMessage("You are in love with " + lovers[0] + ".");
		}
		
		while (true) {
			Thread[] threads = new Thread[names.size()];
			ConcurrentHashMap<String, String> actions = new ConcurrentHashMap<String, String>();
			for (int a = 0; a < names.size(); a++) {
				threads[a] = new Thread(new PlayerWorker(playersByName.get(names.get(a)), names, actions));
				threads[a].start();
			}
			for (Thread t: threads)
				t.join();
			String killed = "";
			int most = 0;
			HashMap<String, Integer> toKill = new HashMap<String, Integer>();
			for (String s: names) {
				Player p = playersByName.get(s);
				if (p instanceof Prostitute) {
					String tName = actions.get(s).split(",")[0];
					Player target = playersByName.get(tName);
					if (target.isMafia && (!(target instanceof Prostitute) || onlyProstitute)) {
						toKill = new HashMap<String, Integer>();
						break;
					}
				}
				if (p.isMafia && !(p instanceof Prostitute)) {
					String target = actions.get(s).split(",")[p instanceof Axe ? 1 : 0];
					toKill.putIfAbsent(target, 0);
					toKill.put(target, toKill.get(target) + 1);
				}
				else if (onlyProstitute && p instanceof Prostitute) {
					p.receiveMessage("Kill someone, acting as the lone remaining mafia member.");
					String target = p.getNightAction(names);
					toKill.put(target, 1);
				}
			}
			for (String s: toKill.keySet()) {
				if (toKill.get(s) > most) {
					most = toKill.get(s);
					killed = s;
				}
				else if (toKill.get(s) == most)
					killed = "";
			}
			Player target = playersByName.get(killed);
			if (target instanceof Traveler && ((Traveler) target).traveling)
				killed = "No one";
			if (killed.equals(proxy)) {
				proxy = "";
				killed = "No one";
				for (String s: names) {
					if (playersByName.get(s) instanceof Alien)
						((Alien) playersByName.get(s)).activated = true;
				}
			}
			if (killed.equals(""))
				killed = "No one";
			
			if (!killed.equals("No one"))
				playersByName.get(killed).alive = false;
			
			threads = new Thread[names.size()];
			ConcurrentHashMap<String, Boolean> secondaryActions = new ConcurrentHashMap<String, Boolean>();
			for (int a = 0; a < names.size(); a++) {
				Player q = playersByName.get(names.get(a));
				threads[a] = new Thread(new PlayerWorker2(q,
						q instanceof Doctor ? "Do you wish to save " + killed + "?" : q instanceof Axe ? "Axe hand?" : ""
						, secondaryActions));
				threads[a].start();
			}
			for (Thread t: threads)
				t.join();
			
			for (String s: names) {
				Player p = playersByName.get(s);
				if (p instanceof Doctor && !killed.equals("No one")) {
					actions.put(s, killed);
				}
				else if (p instanceof Prostitute) {
					p.takeEffect(playersByName.get(actions.get(s)), true);
				}
			}
			
			for (String s: names) {
				Player p = playersByName.get(s);
				String[] targets = actions.get(s).split(",");
				if (!p.disabled && targets.length == (p instanceof Axe ? 3 : 2)) {
					String inductee = targets[targets.length - 1].substring(0, targets[targets.length-1].length() - 1);
					boolean disband = targets[targets.length - 1].charAt(targets[targets.length - 1].length() - 1) == 't';
					p.cultLeader.takeEffect(playersByName.get(inductee), disband);
				}
				if (!p.disabled)
					p.takeEffect(playersByName.get(targets[0]), secondaryActions.get(s));
			}
			
			int numLiving = names.size();
			
			deaths(playersByName, names, lovers, proxy);
			
			for (Player p: playersByName.values()) {
				if (p instanceof Alien)
					p.receiveMessage("You have " + (((Alien) p).activated ? "" : "not ") + "been activated.");
				if (p.name.equals(lovers[0]) && p.inCult)
					playersByName.get(lovers[1]).inCult = true;
				if (p.name.equals(lovers[1]) && p.inCult)
					playersByName.get(lovers[0]).inCult = true;
			}
			
			if (numLiving == names.size())
				System.out.println("No one died.");
			String[] text = new String[names.size() + 1];
			text[0] = "No one";
			for (int a = 0; a < names.size(); a++)
				text[a + 1] = names.get(a);
			panel = new Client.ButtonPanel("Vote to kill", text);
			panel.resetRet();
			while (panel.getRet().equals(""))
				Thread.sleep(300);
			names.remove("No one");
			if (!panel.getRet().equals("No one")) {
				Player curr = playersByName.get(panel.getRet());
				curr.alive = false;
				if (curr instanceof Alien && ((Alien) curr).activated && !curr.inCult) {
					System.out.println(panel.getRet() + " wins!");
					break;
				}
			}
			
			deaths(playersByName, names, lovers, proxy);
			
			boolean mafia = false;
			boolean town = false;
			boolean alien = false;
			boolean cult = false;
			onlyProstitute = true;
			for (Player p: playersByName.values()) {
				if (p.inCult)
					cult = true;
				else if (p.isMafia) {
					mafia = true;
					if (!(p instanceof Prostitute))
						onlyProstitute = false;
				}
				else if (p instanceof Alien)
					alien = true;
				else
					town = true;
			}
			
			if (!mafia) {
				System.out.println("All mafia are dead.");
				if (cult)
					System.out.println("The cult leader is still alive.");
				if (alien)
					System.out.println("The alien is still alive.");
				break;
			}
			else if (!town) {
				System.out.println("The town has perished.");
				if (cult)
					System.out.println("The cult leader is still alive.");
				if (alien)
					System.out.println("The alien is still alive.");
				break;
			}
			else
				System.out.println("Let the night commence.");
		}
		for (Player p: playersByName.values())
			p.receiveMessage("Game over.");
	}
	
	private static void deaths(HashMap<String, Player> playersByName, ArrayList<String> names, String[] lovers, String proxy) {
		for (int s = 0; s < names.size(); s++) {
			Player p = playersByName.get(names.get(s));
			if (!p.alive) {
				System.out.println(p.name + " has died.");
				System.out.println(p.name + " was " + (p.isMafia ? "" : "not ") + "in the mafia.");
				System.out.println(p.name + " was " + (p.inCult ? "" : "not ") + "in the cult.");
				if (p.name.equals(proxy)) {
					System.out.println("The alien has been activated.");
					for (Player y: playersByName.values())
						if (y instanceof Alien)
							((Alien) y).activated = true;
				}
				if (p.name.equals(lovers[0]) || p.name.equals(lovers[1])) {
					String name = lovers[p.name.equals(lovers[0]) ? 1 : 0];
					Player q = playersByName.get(name);
					int ind = names.indexOf(name);
					q.alive = false;
					q.onDeath(playersByName);
					String tmp = names.get(0);
					names.set(0, names.get(ind));
					names.set(ind, tmp);
					if (s == 0)
						s++;
				}
				p.onDeath(playersByName);
				try {
					p.receiveMessage("You have died.");
				} catch (IOException e) {
				}
				playersByName.remove(p.name);
				names.remove(s);
				s--;
			}
		}
		
		for (int s = 0; s < names.size(); s++) {
			Player p = playersByName.get(names.get(s));
			if (!p.alive) {
				System.out.println(p.name + " has died.");
				System.out.println(p.name + " was " + (p.isMafia ? "" : "not ") + "in the mafia.");
				System.out.println(p.name + " was " + (p.inCult ? "" : "not ") + "in the cult.");
				if (p.name.equals(proxy)) {
					System.out.println("The alien has been activated.");
					for (Player y: playersByName.values())
						if (y instanceof Alien)
							((Alien) y).activated = true;
				}
				try {
					p.receiveMessage("You have died.");
				} catch (IOException e) {
				}
				playersByName.remove(p.name);
				names.remove(s);
				s--;
			}
		}
	}
	
	public static class PlayerWorker implements Runnable {

		private Player p;
		private ArrayList<String> names;
		private ConcurrentHashMap<String, String> map;
		
		public PlayerWorker(Player p, ArrayList<String> names, ConcurrentHashMap<String, String> map) {
			this.p = p;
			this.names = names;
			this.map = map;
		}
		
		@Override
		public void run() {
			try {
				map.put(p.name, p.getNightAction(names));
			} catch (IOException e) {
				this.run();
			}
		}
		
	}
	
	public static class PlayerWorker2 implements Runnable {

		private Player p;
		private ConcurrentHashMap<String, Boolean> map;
		private String message;
		
		public PlayerWorker2(Player p, String message, ConcurrentHashMap<String, Boolean> map) {
			this.p = p;
			this.message = message;
			this.map = map;
		}
		
		@Override
		public void run() {
			try {
				map.put(p.name, p.extraNightAction(message));
			} catch (IOException e) {
				this.run();
			}
		}
		
	}
	
}

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CultLeader extends Player {

	public boolean disbanded;
	private ArrayList<Player> cult;
	
	public CultLeader() {
		super();
		className = "Cult Leader";
		inCult = true;
		disbanded = false;
		cult = new ArrayList<Player>();
		cultLeader = this;
	}

	@Override
	public void takeEffect(Player p, boolean action) {
		if (action)
			disbanded = true;
		else if (!(p instanceof Traveler) || !((Traveler) p).traveling) {
			if (p instanceof GrandInquisitor) {
				p.alive = false;
				((GrandInquisitor) p).recruited = true;
				return;
			}
			p.inCult = true;
			try {
				p.receiveMessage("You have been inducted into the cult.");
			} catch (IOException e) {
			}
			cult.add(p);
		}
	}
	
	public String getNightAction(ArrayList<String> names) throws IOException {
		disbanded = false;
		return super.getNightAction(names);
	}

	@Override
	public void onDeath(HashMap<String, Player> players) {
		if (disbanded) {
			while (cult.size() > 0 && !cult.get(0).alive)
				cult.remove(0);
			if (cult.size() == 0)
				return;
			cult.get(0).receiveCultPowers(cultLeader);
			cult.remove(0);
			return;
		}
		for (Player s: cult) {
			if (s.alive)
				s.onDeath(players);
			s.alive = false;
		}
	}
}

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Traveler extends Player {
	
	public boolean traveling;
	
	public Traveler() {
		super();
		traveling = false;
		className = "Traveler";
	}

	@Override
	public void takeEffect(Player p, boolean action) {
		if (p != null)
			return;
		if (traveling)
			traveling = false;
		else
			traveling = action;
	}
	
	public String getNightAction(ArrayList<String> names) throws IOException {
		takeEffect(null, extraNightAction("Do you wish to travel?"));
		return names.get(0);
	}
	
	@Override
	public void onDeath(HashMap<String, Player> players) {
		if (cultLeader != null)
			cultLeader.onDeath(players);
	}

}

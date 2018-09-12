import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GrandInquisitor extends Player {

	public boolean recruited;
	
	public GrandInquisitor() {
		super();
		recruited = false;
		className = "Grand Inquisitor";
	}

	@Override
	public void takeEffect(Player p, boolean action) {
		try {
			super.receiveMessage(p.name + " is " + (p.cultLeader == null ? "not " : "") + "the cult leader.");
		}
		catch (IOException e) {
		}
	}

	@Override
	public void onDeath(HashMap<String, Player> players) {
		if (cultLeader != null)
			cultLeader.onDeath(players);
		ArrayList<String> names = new ArrayList<String>();
		for (String p: players.keySet())
			names.add(p);
		try {
			String dead = super.getNightAction(names);
			if (players.get(dead).cultLeader != null) {
				players.get(dead).alive = false;
				players.get(dead).onDeath(players);
			}
		}
		catch (Exception e) {
		}
	}



}

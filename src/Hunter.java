import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Hunter extends Player {

	public Hunter() {
		super();
		className = "Hunter";
	}

	@Override
	public void takeEffect(Player p, boolean action) {		
	}

	@Override
	public void onDeath(HashMap<String, Player> players) {
		if (cultLeader != null)
			cultLeader.onDeath(players);
		ArrayList<String> names = new ArrayList<String>();
		for (String p: players.keySet())
			if (players.get(p).alive)
				names.add(p);
		try {
			String dead = super.getNightAction(names);
			players.get(dead).alive = false;
			players.get(dead).onDeath(players);
		}
		catch (Exception e) {
		}
	}
	
}

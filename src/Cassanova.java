import java.util.HashMap;

public class Cassanova extends Player {

	public Cassanova() {
		super();
		className = "Cassanova";
	}
	
	@Override
	public void takeEffect(Player p, boolean action) {		
	}

	@Override
	public void onDeath(HashMap<String, Player> players) {
		if (cultLeader != null)
			cultLeader.onDeath(players);
	}

	
}

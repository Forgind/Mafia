import java.util.HashMap;

public class Alien extends Player {

	public boolean activated;
	
	public Alien() {
		super();
		className = "Alien";
		activated = false;
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

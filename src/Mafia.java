import java.util.HashMap;

public class Mafia extends Player {

	public Mafia() {
		super();
		isMafia = true;
		className = "Mafia";
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

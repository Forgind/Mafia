import java.util.HashMap;

public class Prostitute extends Player {

	public Prostitute() {
		super();
		className = "Prostitute";
		isMafia = true;
	}

	@Override
	public void takeEffect(Player p, boolean action) {
		p.disabled = true;
	}

	@Override
	public void onDeath(HashMap<String, Player> players) {
		if (cultLeader != null)
			cultLeader.onDeath(players);
	}
	
}

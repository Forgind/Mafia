import java.util.HashMap;

public class Axe extends Player {

	public Axe() {
		super();
		isMafia = true;
		className = "Axe";
	}

	@Override
	public void takeEffect(Player p, boolean action) {
		System.out.println(p.name + " may not " + (action ? "vote." : "speak."));
	}

	@Override
	public void onDeath(HashMap<String, Player> players) {
		if (cultLeader != null)
			cultLeader.onDeath(players);
	}
	
}

import java.io.IOException;
import java.util.HashMap;

public class Cop extends Player {

	public Cop() {
		super();
		className = "Cop";
	}
	
	@Override
	public void takeEffect(Player p, boolean action) {
		try {
			if (p.isMafia)
				super.receiveMessage(p.name + " is in the mafia.");
			else
				super.receiveMessage(p.name + " is not in the mafia.");
		}
		catch (IOException e) {
		}
	}

	@Override
	public void onDeath(HashMap<String, Player> players) {
		if (cultLeader != null)
			cultLeader.onDeath(players);
	}

}

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Town extends Player {

	public Town() {
		super();
		className = "Town";
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

import java.io.IOException;
import java.util.HashMap;

public class Doctor extends Player {

	private boolean hasSaved;
	
	public Doctor() {
		super();
		className = "Doctor";
		hasSaved = false;
	}
	
	@Override
	public void takeEffect(Player p, boolean action) {
		if (!p.alive && action && !hasSaved) {
			if ((p instanceof GrandInquisitor) && ((GrandInquisitor) p).recruited) {
				try {
					receiveMessage("Your save has not been used. This player was the grand inquisitor and has been recruited into the cult.");
				} catch (IOException e) {
				}
			}
			else {
				hasSaved = true;
				p.alive = true;
			}
		}
	}

	@Override
	public void onDeath(HashMap<String, Player> players) {
		if (cultLeader != null)
			cultLeader.onDeath(players);
	}

}

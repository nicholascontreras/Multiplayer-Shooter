package shared.weapons;

import shared.Weapon;

/**
 * @author Nicholas Contreras
 */

public class AssaultRifle extends Weapon {
	
	static {
		
	}

	@Override
	public String getDisplayName() {
		return "Assault Rifle";
	}

	@Override
	public int getNumUpdatesBetweenShots() {
		return 15;
	}

	@Override
	public int getNumUpdatesForReload() {
		return 120;
	}

	@Override
	public int getMagazineSize() {
		return 10;
	}

	@Override
	public int getBulletDamage() {
		return 5;
	}

	@Override
	public int getMaxRange() {
		return 400;
	}

	@Override
	public String getImageFilename() {
		return null;
	}
}

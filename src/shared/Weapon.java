package shared;

import java.util.HashSet;

/**
* @author Nicholas Contreras
*/

abstract public class Weapon {
	
	public static HashSet<Weapon> displayVersions = new HashSet<Weapon>();			
	
	public abstract String getDisplayName();
	
	public abstract int getNumUpdatesBetweenShots();
	
	public abstract int getNumUpdatesForReload();
	
	public abstract int getMagazineSize();
	
	public abstract int getBulletDamage();
	
	public abstract int getMaxRange();
	
	public abstract String getImageFilename();

}

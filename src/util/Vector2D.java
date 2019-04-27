package util;

/**
 * @author Nicholas Contreras
 */

public class Vector2D {

	private final double x, y;

	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Vector2D add(Vector2D v) {
		return this.add(v.x, v.y);
	}

	public Vector2D add(double x, double y) {
		return new Vector2D(this.x + x, this.y + y);
	}
	
	public Vector2D scale(double scaler) {
		return new Vector2D(x * scaler, y * scaler);
	}

	public double getMag() {
		return Math.hypot(x, y);
	}

	public Vector2D normalize() {
		double mag = getMag();
		return new Vector2D(x / mag, y / mag);
	}
	
	public double dotProd(Vector2D other) {
		return this.x * other.x + this.y * other.y;
	}
}

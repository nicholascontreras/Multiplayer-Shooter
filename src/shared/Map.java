package shared;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import util.Util;
import util.Vector2D;

/**
 * @author Nicholas Contreras
 */

public class Map {

	private BufferedImage image;
	private Set<Wall> walls;

	private int width, height;

	private int team1SpawnX, team1SpawnY, team2SpawnX, team2SpawnY;

	private String dataString;

	public Map(BufferedImage image, InputStreamReader collisions) {

		this.image = image;
		width = image.getWidth();
		height = image.getHeight();

		createWalls(collisions);

		team1SpawnX = 25;
		team1SpawnY = 25;

		team2SpawnX = width - 25;
		team2SpawnY = height - 25;

		dataString = createDataString();
	}

	public Map(String dataString) {

		this.dataString = dataString;

		this.width = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");
		this.height = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");

		String imageString = Util.readUpTo(dataString, ",");
		dataString = Util.removeTo(dataString, ",");

		try {
			image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(imageString)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.team1SpawnX = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");
		this.team1SpawnY = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");
		this.team2SpawnX = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");
		this.team2SpawnY = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");
	}

	private void createWalls(InputStreamReader collisions) {
		String allWalls = "";
		try {
			BufferedReader br = new BufferedReader(collisions);
			String curLine = br.readLine();

			while (curLine != null) {
				allWalls += curLine;
				curLine = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		String[] splitWalls = allWalls.split(";");

		walls = new HashSet<Wall>();

		walls.add(new Wall(0, 0, width, 0));
		walls.add(new Wall(width, 0, width, height));
		walls.add(new Wall(0, height, width, height));
		walls.add(new Wall(0, 0, 0, height));

		for (String curWall : splitWalls) {
			String[] curWallSplit = curWall.split(",");
			int x1 = Integer.parseInt(curWallSplit[0]);
			int y1 = Integer.parseInt(curWallSplit[1]);
			int x2 = Integer.parseInt(curWallSplit[2]);
			int y2 = Integer.parseInt(curWallSplit[3]);
			walls.add(new Wall(x1, y1, x2, y2));
		}
	}

	private String createDataString() {
		StringBuilder sb = new StringBuilder(width + "," + height);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sb.append("," + Base64.getEncoder().encodeToString(baos.toByteArray()));

		sb.append("," + team1SpawnX + "," + team1SpawnY + "," + team2SpawnX + "," + team2SpawnY);

		return sb.toString();
	}

	public int getTeam1SpawnX() {
		return team1SpawnX;
	}

	public int getTeam1SpawnY() {
		return team1SpawnY;
	}

	public int getTeam2SpawnX() {
		return team2SpawnX;
	}

	public int getTeam2SpawnY() {
		return team2SpawnY;
	}

	public String getDataString() {
		return dataString;
	}

	public BufferedImage getImage() {
		return image;
	}

	public Set<Wall> getWalls() {
		return walls;
	}

	public class Wall {
		private int x1, y1, x2, y2;

		public Wall(int x1, int y1, int x2, int y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public int hashCode() {
			return this.x1 + this.y1 + this.x2 + this.y2;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Wall) {
				Wall other = (Wall) obj;
				return this.x1 == other.x1 && this.y1 == other.y1 && this.x2 == other.x2 && this.y2 == other.y2;
			} else {
				return false;
			}
		}

		public boolean intersects(Vector2D point, int radius) {
			return Line2D.ptSegDist(x1, y1, x2, y2, point.getX(), point.getY()) < radius;
		}

		public Vector2D getDirectionVector() {
			return new Vector2D(x2 - x1, y2 - y1).normalize();
		}
	}
}

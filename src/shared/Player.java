package shared;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import server.ShooterServer;
import util.Util;
import util.Vector2D;

/**
 * @author Nicholas Contreras
 */

public class Player {

	private static final int MAX_TIME_WITHOUT_UPDATE = 1000;
	private static int NEXT_ID = 1;

	private int id;

	private String username;

	private int team;
	private boolean isSpawned;

	private long nextSpawnTime;

	private double xPos, yPos;

	private double angle;

	private int drawRadius = 20;

	private double movementSpeed = 2;

	private long lastUpdateTime;

	private Weapon curWeapon;

	public Player() {
		id = NEXT_ID;
		NEXT_ID++;
		renewLastUpdateTime();
	}

	public Player(int id) {
		this.id = id;
	}

	public Player(String dataString) {
		updateFromServerMessage(dataString);
		renewLastUpdateTime();
	}

	public void setWeapon(Weapon weapon) {
		this.curWeapon = weapon;
	}

	public void updateFromClientMessage(String messageFromClient) {

		switch (messageFromClient) {
		case "requestSpawn":
			if (getSecsUntilSpawn() == 0) {
				ShooterServer.getInst().moveToSpawn(this);
				isSpawned = true;
			}
			break;
		default:
			String moveDirection = Util.readUpTo(messageFromClient, ",");
			messageFromClient = Util.removeTo(messageFromClient, ",");
			moveDirection = moveDirection.trim();
			double deltaX = 0, deltaY = 0;
			if (moveDirection.contains("N")) {
				deltaY = -movementSpeed;
			} else if (moveDirection.contains("S")) {
				deltaY = movementSpeed;
			}
			if (moveDirection.contains("W")) {
				deltaX = -movementSpeed;
			} else if (moveDirection.contains("E")) {
				deltaX = movementSpeed;
			}

			if (moveDirection.length() == 2) {
				deltaX /= Math.sqrt(2);
				deltaY /= Math.sqrt(2);
			}

			Vector2D movement = new Vector2D(deltaX, deltaY);
			movement = ShooterServer.getInst().adjustMovementForCollisions(this, movement);

			xPos += movement.getX();
			yPos += movement.getY();

			angle = Double.parseDouble(Util.readUpTo(messageFromClient, ","));
			messageFromClient = Util.removeTo(messageFromClient, ",");
			break;
		}
	}

	public void updateFromServerMessage(String messageFromServer) {
		id = Integer.parseInt(Util.readUpTo(messageFromServer, ","));
		messageFromServer = Util.removeTo(messageFromServer, ",");
		username = Util.readUpTo(messageFromServer, ",");
		messageFromServer = Util.removeTo(messageFromServer, ",");
		team = Integer.parseInt(Util.readUpTo(messageFromServer, ","));
		messageFromServer = Util.removeTo(messageFromServer, ",");
		isSpawned = Boolean.parseBoolean(Util.readUpTo(messageFromServer, ","));
		messageFromServer = Util.removeTo(messageFromServer, ",");
		setSpawnAvaliableIn(Integer.parseInt(Util.readUpTo(messageFromServer, ",")));
		messageFromServer = Util.removeTo(messageFromServer, ",");
		xPos = Double.parseDouble(Util.readUpTo(messageFromServer, ","));
		messageFromServer = Util.removeTo(messageFromServer, ",");
		yPos = Double.parseDouble(Util.readUpTo(messageFromServer, ","));
		messageFromServer = Util.removeTo(messageFromServer, ",");
		angle = Double.parseDouble(Util.readUpTo(messageFromServer, ","));
		messageFromServer = Util.removeTo(messageFromServer, ",");
	}

	public void setSpawnAvaliableIn(int seconds) {
		nextSpawnTime = System.currentTimeMillis() + seconds * 1000;
	}

	public int getSecsUntilSpawn() {
		return Math.max((int) ((nextSpawnTime - System.currentTimeMillis()) / 1000), 0);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public String getSendableForm() {
		return id + "," + username + "," + team + "," + isSpawned + "," + getSecsUntilSpawn() + "," + xPos + "," + yPos
				+ "," + angle;
	}

	public void setSpawned(boolean isSpawned) {
		this.isSpawned = isSpawned;
	}

	public boolean isSpawned() {
		return isSpawned;
	}

	public void setTeam(int team) {
		this.team = team;
	}

	public int getTeam() {
		return this.team;
	}

	public int getDrawRadius() {
		return this.drawRadius;
	}

	public void renewLastUpdateTime() {
		lastUpdateTime = System.currentTimeMillis();
	}

	public boolean isStale() {
		return lastUpdateTime + MAX_TIME_WITHOUT_UPDATE < System.currentTimeMillis();
	}

	public int getID() {
		return id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public void setPosition(double x, double y) {
		xPos = x;
		yPos = y;
	}

	public double getXPos() {
		return xPos;
	}

	public double getYPos() {
		return yPos;
	}

	public void draw(Graphics2D g2d) {
		g2d.translate(xPos, yPos);
		g2d.rotate(Math.toRadians(angle));
		g2d.setColor(Color.BLUE);
		g2d.fillOval(-drawRadius, -drawRadius, drawRadius * 2, drawRadius * 2);

		// if (curWeapon != null) {
		g2d.fillRect(0, drawRadius, drawRadius, drawRadius / 4);
		// }

		g2d.rotate(Math.toRadians(-angle));

		int offset = g2d.getFontMetrics().stringWidth(username) / 2;
		g2d.setColor(Color.RED);
		g2d.drawString(username, -offset, -drawRadius * 2);

		g2d.translate(-xPos, -yPos);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Player) {
			return this.id == ((Player) obj).id;
		} else {
			return false;
		}
	}
}

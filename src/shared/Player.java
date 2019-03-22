package shared;

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

import util.Util;

/**
 * @author Nicholas Contreras
 */

public class Player {

	private static final int MAX_TIME_WITHOUT_UPDATE = 1000;
	private static int NEXT_ID = 1;

	private int id;

	private String username;

	private boolean isSpawned;

	private long nextSpawnTime;

	private double xPos, yPos;

	private double movementSpeed;

	private int team;

	private long lastUpdateTime;

	public Player() {
		id = NEXT_ID;
		NEXT_ID++;
	}

	public void updateFromClientMessage(String messageFromClient) {
		String moveDirection = Util.readUpTo(messageFromClient, ",");

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
			deltaX *= Math.sqrt(2);
			deltaY *= Math.sqrt(2);
		}
	}

	public void updateFromServerMessage(String messageFromServer) {

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
		return id + "," + xPos + "," + yPos;
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

	public boolean isStale() {
		return lastUpdateTime + MAX_TIME_WITHOUT_UPDATE < System.currentTimeMillis();
	}

	@Override
	public int hashCode() {
		return id;
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

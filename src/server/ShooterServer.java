package server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import shared.Map;
import shared.Map.Wall;
import shared.Player;
import util.Util;
import util.Vector2D;

/**
 * @author Nicholas Contreras
 */

public class ShooterServer {

	private static final int MAX_TIME_WITHOUT_USERNAME = 60000;

	private static final int TARGET_UPS = 30;

	private static ShooterServer instance;

	private ServerSocket ss;

	private HashMap<Socket, Long> incomingSockets;
	private HashMap<Player, Socket> connectedPlayers;

	private Map map;

	public static ShooterServer getInst() {
		return instance;
	}

	public static void main(String[] args) {
		new ShooterServer();
	}

	private ShooterServer() {

		System.out.println("Starting Server...");

		instance = this;

		incomingSockets = new HashMap<Socket, Long>();
		connectedPlayers = new HashMap<Player, Socket>();

		map = loadMap("map1");

		try {
			ss = new ServerSocket(0);

			System.out.println("Opened Server Socket Address: " + InetAddress.getLocalHost().getHostAddress()
					+ " Port: " + ss.getLocalPort());

			Thread acceptConnectionsThread = new Thread(() -> acceptNewConnections(), "Accept-Connections-Thread");
			acceptConnectionsThread.start();

			Thread updateClientsThread = new Thread(() -> updateClients(), "Update-Clients-Thread");
			updateClientsThread.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Map loadMap(String mapName) {
		try {
			BufferedImage image = ImageIO.read(this.getClass().getResource("/" + mapName + "Image.png"));
			InputStreamReader collisions = new InputStreamReader(this.getClass().getResourceAsStream("/" + mapName + "Collisions.txt"));

			return new Map(image, collisions);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void acceptNewConnections() {
		while (true) {
			try {
				System.out.println("Waiting for new connection...");
				Socket incomingConnection = ss.accept();
				System.out.println("accepted a new connection");
				System.out.println("remote ip: " + incomingConnection.getRemoteSocketAddress());
				Util.writeSocket(incomingConnection, map.getDataString());
				System.out.println("sent the map to the new connection");
				synchronized (incomingConnection) {
					incomingSockets.put(incomingConnection, System.currentTimeMillis());
					System.out.println("added the connection to the map");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateClients() {
		while (true) {
			long startTime = System.currentTimeMillis();
			updateConnectingPlayers();
			updateConnectedPlayers();
			int elapsedTime = (int) (System.currentTimeMillis() - startTime);
			try {
				Thread.sleep(Math.max((1000 / TARGET_UPS) - elapsedTime, 0));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateConnectingPlayers() {

		synchronized (incomingSockets) {
			HashSet<Socket> socketsToRemove = new HashSet<Socket>();
			for (Socket curSocket : incomingSockets.keySet()) {
				String message = Util.readSocket(curSocket);

				if (message != null) {
					String problem = getProblemWithUsername(message);

					if (problem == null) {
						Player player = new Player();
						player.setUsername(message);
						player.setTeam(getSmallerTeam());
						player.setSpawnAvaliableIn(1);

						socketsToRemove.add(curSocket);

						synchronized (connectedPlayers) {
							connectedPlayers.put(player, curSocket);
						}
						Util.writeSocket(curSocket, "approved:" + player.getID());
					} else {
						Util.writeSocket(curSocket, problem);
						incomingSockets.put(curSocket, System.currentTimeMillis());
					}
				} else {
					if (incomingSockets.get(curSocket) + MAX_TIME_WITHOUT_USERNAME < System.currentTimeMillis()) {
						Util.closeSocket(curSocket);
						socketsToRemove.add(curSocket);
					}
				}
			}
			for (Socket curSocket : socketsToRemove) {
				incomingSockets.remove(curSocket);
			}
		}
	}

	private void updateConnectedPlayers() {

		String updateMessage = "update:";
		HashSet<Player> playersToRemove = new HashSet<Player>();
		synchronized (connectedPlayers) {
			for (Player curPlayer : connectedPlayers.keySet()) {
				String messageFromClient = Util.readSocket(connectedPlayers.get(curPlayer));

				while (messageFromClient != null) {
					curPlayer.renewLastUpdateTime();
					if (!messageFromClient.equals("keepAlive")) {
						curPlayer.updateFromClientMessage(messageFromClient);
					}
					messageFromClient = Util.readSocket(connectedPlayers.get(curPlayer));
				}
				updateMessage += curPlayer.getSendableForm() + ";";
			}

			for (Player curPlayer : connectedPlayers.keySet()) {
				Util.writeSocket(connectedPlayers.get(curPlayer), updateMessage);

				if (curPlayer.isStale()) {
					Util.closeSocket(connectedPlayers.get(curPlayer));
					playersToRemove.add(curPlayer);
				}
			}

			for (Player curPlayer : playersToRemove) {
				connectedPlayers.remove(curPlayer);
				System.out.println("Removed player " + curPlayer.getID() + " because the connection was stale");
			}
		}

	}

	private String getProblemWithUsername(String username) {

		char[] forbidenChars = { ',', ':', ';' };

		for (char curChar : forbidenChars) {
			if (username.contains(curChar + "")) {
				return "Character " + curChar + " is not allowed in usernames";
			}
		}

		if (username.length() < 2) {
			return "Username is too short";
		} else if (username.length() > 12) {
			return "Username is too long";
		}

		for (Player curPlayer : connectedPlayers.keySet()) {
			if (curPlayer.getUsername().equals(username)) {
				return "Username " + username + " is already taken";
			}
		}
		return null;
	}

	private int getSmallerTeam() {
		double teamSum = 0;
		for (Player curPlayer : connectedPlayers.keySet()) {
			teamSum += curPlayer.getTeam();
		}
		int largerTeam = (int) Math.round(teamSum / connectedPlayers.size());
		return largerTeam == 1 ? 2 : 1;
	}
	
	public Vector2D adjustMovementForCollisions(Player player, Vector2D movement) {
		
		Vector2D newLoc = movement.add(player.getXPos(), player.getYPos());
		
		for (Wall curWall : map.getWalls()) {
			if (curWall.intersects(newLoc, player.getDrawRadius())) {
				Vector2D wallDir = curWall.getDirectionVector();
				movement = wallDir.scale(movement.dotProd(wallDir));
			}
		}
		
		return movement;
	}

	public void moveToSpawn(Player player) {
		if (player.getTeam() == 1) {
			player.setPosition(map.getTeam1SpawnX(), map.getTeam1SpawnY());
		} else {
			player.setPosition(map.getTeam2SpawnX(), map.getTeam2SpawnY());
		}
	}
}

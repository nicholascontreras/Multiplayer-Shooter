package server;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;

import shared.Map;
import shared.Player;
import util.Util;

/**
 * @author Nicholas Contreras
 */

public class ShooterServer {

	private static final int MAX_TIME_WITHOUT_USERNAME = 60000;

	private ServerSocket ss;

	private HashMap<Socket, Long> incomingSockets;
	private HashMap<Player, Socket> connectedPlayers;

	private Map map;

	public static void main(String[] args) {
		new ShooterServer();
	}

	private ShooterServer() {

		System.out.println("Starting Server...");

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
			BufferedImage mesh = ImageIO.read(this.getClass().getResource("/" + mapName + "Mesh.png"));

			return new Map(image, mesh);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void acceptNewConnections() {
		while (true) {
			try {
				Socket incomingConnection = ss.accept();
				Util.writeSocket(incomingConnection, map.getDataString());
				synchronized (incomingConnection) {
					incomingSockets.put(incomingConnection, System.currentTimeMillis());
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
			updateConnectingPlayers();
			updateConnectedPlayers();
			try {
				Thread.sleep(20);
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
						player.setSpawnAvaliableIn(20);

						socketsToRemove.add(curSocket);

						synchronized (connectedPlayers) {
							connectedPlayers.put(player, curSocket);
						}
						Util.writeSocket(curSocket, "approved");
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

				if (messageFromClient != null) {

					switch (messageFromClient) {
					case "spawnTime":
						Util.writeSocket(connectedPlayers.get(curPlayer), curPlayer.getSecsUntilSpawn() + "");
						break;
					case "spawn":
						curPlayer.setSpawned(true);
						break;
					default:
						curPlayer.updateFromClientMessage(messageFromClient);
						break;
					}
				}
				updateMessage += curPlayer.getSendableForm() + ";";
			}

			updateMessage = updateMessage.substring(0, updateMessage.length() - 1);

			for (Player curPlayer : connectedPlayers.keySet()) {
				Util.writeSocket(connectedPlayers.get(curPlayer), updateMessage);

				if (curPlayer.isStale()) {
					Util.closeSocket(connectedPlayers.get(curPlayer));
					playersToRemove.add(curPlayer);
				}
			}

			for (Player curPlayer : playersToRemove) {
				connectedPlayers.remove(curPlayer);
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
}

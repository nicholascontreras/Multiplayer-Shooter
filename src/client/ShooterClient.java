package client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import client.gamePanels.GamePanel;
import client.gamePanels.MainMenuPanel;
import client.gamePanels.SpawnMenuPanel;
import shared.Map;
import shared.Player;

/**
 * @author Nicholas Contreras
 */

public class ShooterClient implements Runnable {

	private static final int WINDOW_SIZE = 800;

	private static JFrame frame;
	private static JPanel outerPanel;
	private static Runnable curPanelRunning;

	private static HashMap<String, JPanel> gamePanels;

	private static Map map;

	private static int localID;
	private static ArrayList<Player> players;

	private static Socket socket;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new ShooterClient());
	}

	public void run() {
		setUIFont(new FontUIResource("SansSerif", Font.PLAIN, 24));

		players = new ArrayList<Player>();

		gamePanels = new HashMap<String, JPanel>();
		gamePanels.put("MainMenu", new MainMenuPanel());
		gamePanels.put("SpawnMenu", new SpawnMenuPanel());
		gamePanels.put("Game", new GamePanel());

		frame = new JFrame("Multiplayer Shooter");

		outerPanel = new JPanel(new CardLayout());
		outerPanel.setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));

		for (String key : gamePanels.keySet()) {
			outerPanel.add(gamePanels.get(key), key);
		}

		frame.add(outerPanel);

		InputManager.addListeners(frame);

		switchPanel("MainMenu");

		frame.pack();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setVisible(true);

		Thread t = new Thread(() -> {
			while (true) {
				curPanelRunning.run();
			}
		}, "Update-Thread");
		t.start();
	}

	public static void switchPanel(String panelName) {
		((CardLayout) outerPanel.getLayout()).show(outerPanel, panelName);
		curPanelRunning = (Runnable) gamePanels.get(panelName);
	}

	public static void setSocket(Socket s) {
		socket = s;
	}

	public static Socket getSocket() {
		return socket;
	}

	public static void setMap(Map m) {
		map = m;
	}

	public static Map getMap() {
		return map;
	}

	public static void updateGameFromMessage(String message) {
		message = message.substring("update:".length());
		String[] splitMessage = message.split(";");

		for (String playerData : splitMessage) {
			int curPlayerID = Integer.parseInt(playerData.substring(0, playerData.indexOf(",")));

			boolean foundPlayer = false;
			synchronized (players) {
				for (Player p : players) {
					if (p.getID() == curPlayerID) {
						p.updateFromServerMessage(playerData);
						p.renewLastUpdateTime();
						foundPlayer = true;
						break;
					}
				}

				if (!foundPlayer) {
					Player newPlayer = new Player(playerData);
					players.add(newPlayer);
				}
			}
		}

		HashSet<Player> playersToRemove = new HashSet<Player>();
		synchronized (players) {
			for (Player p : players) {
				if (p.isStale()) {
					playersToRemove.add(p);
				}
			}
			for (Player p : playersToRemove) {
				players.remove(p);
				System.out.println("Removed player " + p.getID() + " because the connection was stale");
			}
		}
	}

	public static void setLocalID(int id) {
		localID = id;
	}

	public static boolean isLocalPlayerConnected() {
		synchronized (players) {
			for (Player p : players) {
				if (p.getID() == localID) {
					return true;
				}
			}
		}
		return false;
	}

	public static Player getLocalPlayer() {
		synchronized (players) {
			for (Player p : players) {
				if (p.getID() == localID) {
					return p;
				}
			}
		}
		return null;
	}
	
	public static ArrayList<Player> getPlayers() {
		return players;
	}

	private static void setUIFont(FontUIResource f) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource)
				UIManager.put(key, f);
		}
	}
}

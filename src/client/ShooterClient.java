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
import java.util.HashMap;

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

	private static Socket socket;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new ShooterClient());
	}

	public void run() {
		setUIFont(new FontUIResource("SansSerif", Font.PLAIN, 24));

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

	private static void setUIFont(FontUIResource f) {
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
	}
}

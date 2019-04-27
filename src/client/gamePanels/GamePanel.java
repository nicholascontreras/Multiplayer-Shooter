package client.gamePanels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import client.InputManager;
import client.ShooterClient;
import shared.Map;
import shared.Player;
import util.Util;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class GamePanel extends JPanel implements Runnable {

	private static final int TARGET_FPS = 30;

	private static int PLAYER_FIXED_X, PLAYER_FIXED_Y;

	public GamePanel() {
		this.setPreferredSize(new Dimension(800, 800));

		PLAYER_FIXED_X = 800 / 2;
		PLAYER_FIXED_Y = 800 * 4 / 5;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Player localPlayer = ShooterClient.getLocalPlayer();
		g2d.translate(-localPlayer.getXPos() + PLAYER_FIXED_X, -localPlayer.getYPos() + PLAYER_FIXED_Y);
		g2d.drawImage(ShooterClient.getMap().getImage(), 0, 0, null);

		g2d.setColor(Color.BLUE);

		ArrayList<Player> players = ShooterClient.getPlayers();
		synchronized (players) {
			for (Player p : players) {
				if (p.isSpawned()) {
					p.draw(g2d);
				}
			}
		}

		g2d.translate(localPlayer.getXPos() - PLAYER_FIXED_X, localPlayer.getYPos() - PLAYER_FIXED_Y);
	}

	private void updateServerWithInput() {
		String serverUpdateString = "";

		if (InputManager.isKeyPressed(KeyEvent.VK_W)) {
			serverUpdateString += "N";
		} else if (InputManager.isKeyPressed(KeyEvent.VK_S)) {
			serverUpdateString += "S";
		}
		if (InputManager.isKeyPressed(KeyEvent.VK_A)) {
			serverUpdateString += "W";
		} else if (InputManager.isKeyPressed(KeyEvent.VK_D)) {
			serverUpdateString += "E";
		}
		serverUpdateString += ",";

		int mouseX = InputManager.getMouseX() - PLAYER_FIXED_X;
		int mouseY = InputManager.getMouseY() - PLAYER_FIXED_Y;
		double angle = Math.toDegrees(Math.atan2(mouseY, mouseX));
		serverUpdateString += angle + ",";

		System.out.println("updated server: " + serverUpdateString);

		Util.writeSocket(ShooterClient.getSocket(), serverUpdateString);
	}

	@Override
	public void run() {

		long startTime = System.currentTimeMillis();

		String messageFromServer = Util.readSocket(ShooterClient.getSocket());
		while (messageFromServer != null) {
			if (messageFromServer.startsWith("update:")) {
				ShooterClient.updateGameFromMessage(messageFromServer.substring("update:".length()));
			}
			messageFromServer = Util.readSocket(ShooterClient.getSocket());
		}

		updateServerWithInput();
		repaint();

		int elapsedTime = (int) (System.currentTimeMillis() - startTime);
		try {
			Thread.sleep(Math.max((1000 / TARGET_FPS) - elapsedTime, 0));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

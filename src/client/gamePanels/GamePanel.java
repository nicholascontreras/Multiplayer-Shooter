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

	public GamePanel() {
		this.setPreferredSize(new Dimension(800, 800));
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Player localPlayer = ShooterClient.getLocalPlayer();
		g2d.translate(-localPlayer.getXPos() + getWidth() / 2, -localPlayer.getYPos() + getHeight() / 2);
		g2d.drawImage(ShooterClient.getMap().getImage(), 0, 0, null);

		g2d.setColor(Color.BLUE);

		ArrayList<Player> players = ShooterClient.getPlayers();
		synchronized (players) {
			for (Player p : players) {
				if (p.isSpawned()) {
					int drawRadius = p.getDrawRadius();
					g2d.setColor(Color.BLUE);
					g2d.fillOval((int) p.getXPos() - drawRadius, (int) p.getYPos() - drawRadius, drawRadius * 2,
							drawRadius * 2);
					g2d.setColor(Color.RED);
					int offset = g2d.getFontMetrics().stringWidth(p.getUsername()) / 2;
					g2d.drawString(p.getUsername(), (int) p.getXPos() - drawRadius - offset,
							(int) p.getYPos() - drawRadius * 2);
				}
			}
		}

		g2d.translate(localPlayer.getXPos() - getWidth() / 2, localPlayer.getYPos() - getHeight() / 2);
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

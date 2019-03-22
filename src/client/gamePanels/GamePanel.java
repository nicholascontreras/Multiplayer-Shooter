package client.gamePanels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import client.ShooterClient;
import shared.Map;
import shared.Player;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class GamePanel extends JPanel implements Runnable {

	private static final int PLAYER_SIZE = 20;

	public GamePanel() {
		this.setPreferredSize(new Dimension(800, 800));
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		g2d.drawImage(ShooterClient.getMap().getImage(), 0, 0, null);

		g2d.setColor(Color.BLUE);

		ArrayList<Player> players = ShooterClient.getPlayers();
		synchronized (players) {
			for (Player p : players) {
				g2d.drawOval((int) (p.getXPos() - PLAYER_SIZE / 2), (int) (p.getYPos() - PLAYER_SIZE / 2), PLAYER_SIZE,
						PLAYER_SIZE);
			}
		}
	}

	@Override
	public void run() {

		repaint();

		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

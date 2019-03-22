package client.gamePanels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import client.ShooterClient;
import util.Util;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class SpawnMenuPanel extends JPanel implements Runnable {

	private boolean waitingToSpawn;

	private JButton spawnButton;

	public SpawnMenuPanel() {
		this.setLayout(new BorderLayout());

		spawnButton = new JButton(">>> Spawn <<<");
		spawnButton.addActionListener((ActionEvent e) -> {
			waitingToSpawn = !waitingToSpawn;

			if (!waitingToSpawn) {
				spawnButton.setText(">>> Spawn <<<");
			}
		});
		this.add(spawnButton, BorderLayout.SOUTH);

	}

	@Override
	public void run() {
		Util.writeSocket(ShooterClient.getSocket(), "keepAlive");

		String msg = Util.readSocket(ShooterClient.getSocket());

		if (msg != null) {
			ShooterClient.updateGameFromMessage(msg);
			if (waitingToSpawn) {
				Util.writeSocket(ShooterClient.getSocket(), "requestSpawn");
				int timeTillSpawn = ShooterClient.getLocalPlayer().getSecsUntilSpawn();
				spawnButton.setText("Spawning in: " + timeTillSpawn);

				if (ShooterClient.getLocalPlayer().isSpawned()) {
					ShooterClient.switchPanel("Game");
					waitingToSpawn = false;
					spawnButton.setText(">>> Spawn <<<");
				}
			}
		}

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

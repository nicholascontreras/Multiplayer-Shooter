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
		String messageFromServer = Util.readSocket(ShooterClient.getSocket());

		while (messageFromServer != null) {
			if (messageFromServer.startsWith("update:")) {
				ShooterClient.updateGameFromMessage(messageFromServer.substring("update:".length()));
			}
			messageFromServer = Util.readSocket(ShooterClient.getSocket());
		}

		if (waitingToSpawn) {
			Util.writeSocket(ShooterClient.getSocket(), "requestSpawn");
			int timeTillSpawn = ShooterClient.getLocalPlayer().getSecsUntilSpawn();
			spawnButton.setText("Spawning in: " + timeTillSpawn);

			if (ShooterClient.getLocalPlayer().isSpawned()) {
				ShooterClient.switchPanel("Game");
				waitingToSpawn = false;
				spawnButton.setText(">>> Spawn <<<");

			}
		} else {
			Util.writeSocket(ShooterClient.getSocket(), "keepAlive");
		}

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

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
			System.out.println("button pressed");
			waitingToSpawn = !waitingToSpawn;

			if (!waitingToSpawn) {
				spawnButton.setText(">>> Spawn <<<");
			}
		});
		this.add(spawnButton, BorderLayout.SOUTH);

	}

	@Override
	public void run() {
		if (waitingToSpawn) {
			System.out.println("asked server for spawntime");
			Util.writeSocket(ShooterClient.getSocket(), "spawnTime");
			int timeTillSpawn = Integer.parseInt(Util.waitForeverUntilRead(ShooterClient.getSocket()));

			if (timeTillSpawn == 0) {
				Util.writeSocket(ShooterClient.getSocket(), "spawn");
				ShooterClient.switchPanel("Game");
				waitingToSpawn = false;
				spawnButton.setText(">>> Spawn <<<");
			} else {
				spawnButton.setText("Spawning in: " + timeTillSpawn);
			}
		}

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

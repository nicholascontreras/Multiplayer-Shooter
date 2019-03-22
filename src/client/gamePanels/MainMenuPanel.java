package client.gamePanels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import client.ShooterClient;
import shared.Map;
import util.Util;

/**
 * @author Nicholas Contreras
 */

@SuppressWarnings("serial")
public class MainMenuPanel extends JPanel implements Runnable {

	public MainMenuPanel() {

		JPanel mainMenuOuterPanelHorizontal = new JPanel();
		mainMenuOuterPanelHorizontal.setLayout(new BoxLayout(mainMenuOuterPanelHorizontal, BoxLayout.X_AXIS));

		JPanel mainMenuOuterPanelVertical = new JPanel();
		mainMenuOuterPanelVertical.setLayout(new BoxLayout(mainMenuOuterPanelVertical, BoxLayout.Y_AXIS));

		JPanel mainMenuButtonPanel = new JPanel();
		mainMenuButtonPanel.setLayout(new GridLayout(0, 1, 5, 5));

		JButton joinGameButton = new JButton("Join Game");
		joinGameButton.addActionListener((ActionEvent e) -> {
			showConnectWindow();
		});
		mainMenuButtonPanel.add(joinGameButton);

		JButton createGameButton = new JButton("Create Game");
		mainMenuButtonPanel.add(createGameButton);

		mainMenuOuterPanelVertical.add(Box.createVerticalGlue());
		mainMenuOuterPanelVertical.add(mainMenuButtonPanel);
		mainMenuOuterPanelVertical.add(Box.createVerticalGlue());

		mainMenuOuterPanelHorizontal.add(Box.createHorizontalGlue());
		mainMenuOuterPanelHorizontal.add(mainMenuOuterPanelVertical);
		mainMenuOuterPanelHorizontal.add(Box.createHorizontalGlue());

		this.add(mainMenuOuterPanelHorizontal);
	}

	private void showConnectWindow() {
		JDialog dialog = new JDialog();
		dialog.setTitle("Connect to Game");

		JPanel connectMenuPanel = new JPanel(new BorderLayout(5, 5));
		connectMenuPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel connectMenuFieldsPanel = new JPanel(new GridLayout(0, 2, 5, 5));

		JLabel connectMenuLabel = new JLabel("Enter Server Information");
		connectMenuLabel.setHorizontalAlignment(SwingConstants.CENTER);
		connectMenuPanel.add(connectMenuLabel, BorderLayout.NORTH);

		JLabel serverAddressFieldLabel = new JLabel("Address:");
		connectMenuFieldsPanel.add(serverAddressFieldLabel);

		JTextField serverAddressField = new JTextField();
		serverAddressField.setColumns(15);
		serverAddressField.setHorizontalAlignment(SwingConstants.CENTER);
		connectMenuFieldsPanel.add(serverAddressField);

		JLabel serverPortFieldLabel = new JLabel("Port:");
		connectMenuFieldsPanel.add(serverPortFieldLabel);

		JSpinner serverPortField = new JSpinner(new SpinnerNumberModel(1, 1, 65535, 1));
		JSpinner.NumberEditor ne = new JSpinner.NumberEditor(serverPortField, "#");
		ne.getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		serverPortField.setEditor(ne);
		connectMenuFieldsPanel.add(serverPortField);

		connectMenuPanel.add(connectMenuFieldsPanel, BorderLayout.CENTER);

		JButton connectButton = new JButton("Connect!");
		connectButton.addActionListener((ActionEvent e) -> {
			boolean connected = attemptToConnect(serverAddressField.getText(), (int) serverPortField.getValue());
			if (connected) {
				if (selectUsername()) {
					dialog.dispose();
					ShooterClient.switchPanel("SpawnMenu");
				}
			}
		});
		connectMenuPanel.add(connectButton, BorderLayout.SOUTH);

		dialog.add(connectMenuPanel);
		dialog.pack();
		dialog.setResizable(false);
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setLocationRelativeTo(this);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				dialog.setVisible(true);
			}
		}, 0);

	}

	private boolean attemptToConnect(String address, int port) {
		ShooterClient.setSocket(Util.attemptToConnect(address, port));

		if (ShooterClient.getSocket() == null) {
			JOptionPane.showMessageDialog(this, "Unable to connect to the server", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		String mapData = Util.waitUntilRead(ShooterClient.getSocket(), 10000);

		if (mapData == null) {
			JOptionPane.showMessageDialog(this, "Unable to retrive session info from server", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		ShooterClient.setMap(new Map(mapData));
		return true;
	}

	private boolean selectUsername() {
		while (true) {
			String username = "";
			while (username.isEmpty()) {
				username = JOptionPane.showInputDialog(this, "Enter username:", "Connecting to Server",
						JOptionPane.PLAIN_MESSAGE);
				if (username == null) {
					return false;
				}
			}

			Util.writeSocket(ShooterClient.getSocket(), username);
			String usernameResponse = Util.waitUntilRead(ShooterClient.getSocket(), 5000);

			if (usernameResponse.startsWith("approved:")) {
				ShooterClient
						.setLocalID(Integer.parseInt(usernameResponse.substring(usernameResponse.indexOf(":") + 1)));
				break;
			} else {
				JOptionPane.showMessageDialog(this,
						"Username was rejected for the following reason:" + System.lineSeparator() + usernameResponse,
						"Invalid Username", JOptionPane.ERROR_MESSAGE);
			}
		}
		return true;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

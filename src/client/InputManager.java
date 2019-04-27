package client;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;

import javax.swing.JPanel;

/**
 * @author Nicholas Contreras
 */

public class InputManager {

	private static final HashSet<Integer> KEYS_PRESSED = new HashSet<Integer>();

	private static int MOUSE_X, MOUSE_Y;

	private static boolean LEFT_CLICK, RIGHT_CLICK;

	private static Component LISTENING;

	public static boolean isKeyPressed(int keyCode) {
		return KEYS_PRESSED.contains(keyCode);
	}

	public static int getMouseX() {
		return MOUSE_X;
	}

	public static int getMouseY() {
		return MOUSE_Y;
	}

	public static boolean isLeftClick() {
		return LEFT_CLICK;
	}

	public static boolean isRightClick() {
		return RIGHT_CLICK;
	}

	public static void addListeners(Component component) {

		LISTENING = component;

		component.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				KEYS_PRESSED.add(e.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent e) {
				KEYS_PRESSED.remove(e.getKeyCode());
			}
		});

		component.addMouseListener(new MouseListener() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					LEFT_CLICK = true;
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					RIGHT_CLICK = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					LEFT_CLICK = false;
				}
				if (e.getButton() == MouseEvent.BUTTON3) {
					RIGHT_CLICK = false;
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		component.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				MOUSE_X = e.getX();
				MOUSE_Y = e.getY();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				MOUSE_X = e.getX();
				MOUSE_Y = e.getY();
			}
		});
	}
}

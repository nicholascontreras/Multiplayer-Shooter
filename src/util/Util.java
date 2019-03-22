package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * @author Nicholas Contreras
 */

public class Util {

	public static int[] convertColorIntToRGB(int rgb) {
		int red = (rgb & 0x00ff0000) >> 16;
		int green = (rgb & 0x0000ff00) >> 8;
		int blue = rgb & 0x000000ff;
		return new int[] { red, green, blue };
	}

	public static int convertRGBToColorInt(int r, int g, int b) {
		return ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff);
	}

	public static String readUpTo(String data, String stop) {
		if (data.contains(stop)) {
			int stopIndex = data.indexOf(stop);
			return data.substring(0, stopIndex);
		} else {
			return data;
		}
	}

	public static Socket attemptToConnect(String address, int port) {
		try {
			Socket socket = new Socket(address, port);
			return socket;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String removeTo(String data, String stop) {
		if (data.contains(stop)) {
			int stopIndex = data.indexOf(stop) + stop.length();
			return data.substring(stopIndex);
		} else {
			return null;
		}
	}

	public static String readSocket(Socket socket) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			return br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String waitForeverUntilRead(Socket socket) {
		return waitUntilRead(socket, Integer.MAX_VALUE);
	}

	public static String waitUntilRead(Socket socket, int timeout) {
		long startTime = System.currentTimeMillis();

		while (startTime + timeout > System.currentTimeMillis()) {
			String read = readSocket(socket);

			if (read != null) {
				return read;
			}

			try {
				Thread.sleep(timeout / 10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void writeSocket(Socket socket, String message) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			bw.write(message);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void closeSocket(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

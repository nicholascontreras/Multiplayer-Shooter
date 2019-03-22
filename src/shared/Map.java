package shared;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import util.Util;

/**
 * @author Nicholas Contreras
 */

public class Map {

	private BufferedImage image;
	private byte[] terrainMesh;

	private int width, height;

	private int team1SpawnX, team1SpawnY, team2SpawnX, team2SpawnY;

	private String dataString;

	public Map(BufferedImage image, BufferedImage mesh) {

		if (image.getWidth() != mesh.getWidth() || image.getHeight() != mesh.getHeight()) {
			throw new IllegalArgumentException("Image and mesh size must be the same");
		}

		this.image = image;
		width = image.getWidth();
		height = image.getHeight();

		convertImgToMesh(mesh);

		team1SpawnX = 25;
		team1SpawnY = 25;

		team2SpawnX = width - 25;
		team2SpawnY = height - 25;

		dataString = createDataString();
	}

	public Map(String dataString) {

		this.dataString = dataString;

		this.width = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");
		this.height = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");

		String imageString = Util.readUpTo(dataString, ",");
		dataString = Util.removeTo(dataString, ",");

		try {
			image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(imageString)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String terrainMeshString = Util.readUpTo(dataString, ",");
		dataString = Util.removeTo(dataString, ",");
		terrainMesh = Base64.getDecoder().decode(terrainMeshString);

		this.team1SpawnX = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");
		this.team1SpawnY = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");
		this.team2SpawnX = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");
		this.team2SpawnY = Integer.parseInt(Util.readUpTo(dataString, ","));
		dataString = Util.removeTo(dataString, ",");
	}

	private String createDataString() {
		StringBuilder sb = new StringBuilder(width + "," + height);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sb.append("," + Base64.getEncoder().encodeToString(baos.toByteArray()));

		sb.append("," + Base64.getEncoder().encodeToString(terrainMesh));

		sb.append("," + team1SpawnX + "," + team1SpawnY + "," + team2SpawnX + "," + team2SpawnY);

		return sb.toString();
	}

	private void convertImgToMesh(BufferedImage mesh) {

		terrainMesh = new byte[mesh.getWidth() * mesh.getHeight()];

		int[] meshRGBArray = mesh.getRGB(0, 0, mesh.getWidth(), mesh.getHeight(), null, 0, mesh.getWidth());

		int curX = 0, curY = 0;

		for (int curRGB : meshRGBArray) {
			int[] rgb = Util.convertColorIntToRGB(curRGB);
			byte darkness = (byte) ((rgb[0] + rgb[1] + rgb[2] / 3) + Byte.MIN_VALUE);
			terrainMesh[curX + curY * mesh.getWidth()] = darkness;

			curX++;
			if (curX == mesh.getWidth()) {
				curY++;
				curX = 0;
			}
		}
	}

	public int getTeam1SpawnX() {
		return team1SpawnX;
	}

	public int getTeam1SpawnY() {
		return team1SpawnY;
	}

	public int getTeam2SpawnX() {
		return team2SpawnX;
	}

	public int getTeam2SpawnY() {
		return team2SpawnY;
	}

	public String getDataString() {
		return dataString;
	}

	public BufferedImage getImage() {
		return image;
	}

	public byte[] getTerrainMesh() {
		return terrainMesh;
	}
}

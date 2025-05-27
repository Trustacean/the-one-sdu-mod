/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package gui.playfield;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.*;

import core.Connection;
import core.Coord;
import core.DTNHost;
import core.NetworkInterface;
import lombok.Setter;

/**
 * Visualization of a DTN Node
 */
public class NodeGraphic extends PlayFieldGraphic {
	/**
	 * -- SETTER --
	 * Sets whether radio coverage of nodes should be drawn
	 *
	 * @param draw If true, radio coverage is drawn
	 */
	@Setter
	private static boolean drawCoverage = true;
	/**
	 * -- SETTER --
	 * Sets whether node's name should be displayed
	 *
	 * @param draw If true, node's name is displayed
	 */
	@Setter
	private static boolean drawNodeName = true;
	/**
	 * -- SETTER --
	 * Sets whether node's connections to other nodes should be drawn
	 *
	 * @param draw If true, node's connections to other nodes is drawn
	 */
	@Setter
	private static boolean drawConnections = true;

	private static Color rangeColor = Color.GREEN;
	private static Color conColor = Color.BLACK;
	private static Color hostColor = Color.BLUE;
	private static Color hostNameColor = Color.BLUE;
	private static Color msgColor1 = Color.BLUE;
	private static Color msgColor2 = Color.GREEN;
	private static Color msgColor3 = Color.RED;

	/* Path tracing static variables */

	/**
	 * This variable is used to determine if the trails should be processed (not only drawn).
	 */
	private static Boolean processTrails = false;
	private static Color defaultTrailColor = Color.GRAY;
	private static int trailMaxLength = 50;
	private static boolean isTrailEnabled = true;
	private static boolean isTrailFadeEnabled = true;
	private static boolean isTrailConfigured = false;

	/* Class's trail variables */
	private Color trailColor;
	private List<Coord> positionHistory;

	private static int instanceCounter = 0;
	private int instanceId;

	private final DTNHost node;

	public NodeGraphic(DTNHost node) {
		this.node = node;
		this.instanceId = instanceCounter++;

//		System.out.println("Creating NodeGraphic instance " + instanceId + " for node " + node);

		if (processTrails) {
			this.positionHistory = new LinkedList<>();
			this.trailColor = generateRandomColor();
		} else {
			this.positionHistory = null;
			this.trailColor = defaultTrailColor;
		}
	}

	@Override
	public void draw(Graphics2D g2) {
		drawHost(g2);
		drawMessages(g2);
	}

	/**
	 * Visualize node's location, radio ranges and connections
	 *
	 * @param g2 The graphic context to draw to
	 */
	private void drawHost(Graphics2D g2) {
		Coord loc = node.getLocation();

		if (processTrails && isTrailConfigured) {
			updateAndDrawTrail(g2);
		}

		if (drawCoverage && node.isActive()) {
			ArrayList<NetworkInterface> interfaces = new ArrayList<NetworkInterface>();
			interfaces.addAll(node.getInterfaces());
			for (NetworkInterface ni : interfaces) {
				double range = ni.getTransmitRange();
				Ellipse2D.Double coverage;

				coverage = new Ellipse2D.Double(scale(loc.getX() - range),
					scale(loc.getY() - range), scale(range * 2), scale(range * 2));

				// draw the "range" circle
				g2.setColor(rangeColor);
				g2.draw(coverage);
			}
		}

		if (drawConnections) {
			g2.setColor(conColor);
			Coord c1 = node.getLocation();
			// create a copy to prevent concurrent modification exceptions
			ArrayList<Connection> conList = new ArrayList<>(node.getConnections());
			for (Connection c : conList) {
				Coord c2 = c.getOtherNode(node).getLocation();

				g2.drawLine(scale(c1.getX()), scale(c1.getY()),
					scale(c2.getX()), scale(c2.getY()));
			}
		}

		g2.setColor(hostColor);    // draw rectangle to host's location
		g2.drawRect(scale(loc.getX() - 1), scale(loc.getY() - 1), scale(2), scale(2));

		if (drawNodeName) {
			if (node.getColor() == null) {
				g2.setColor(hostNameColor);
			}
			else {
					g2.setColor(new Color(node.getColor()[0], node.getColor()[1], node.getColor()[2]));
			}
			// Draw node's address next to it
			g2.drawString(node.toString(), scale(loc.getX()),
				scale(loc.getY()));
		}
	}


	/**
	 * Visualize the messages this node is carrying
	 *
	 * @param g2 The graphic context to draw to
	 */
	private void drawMessages(Graphics2D g2) {
		int nrofMessages = node.getNrofMessages();
		Coord loc = node.getLocation();

		drawBar(g2, loc, nrofMessages % 10, 1);
		drawBar(g2, loc, nrofMessages / 10, 2);
	}

	/**
	 * Draws a bar (stack of squares) next to a location
	 *
	 * @param g2   The graphic context to draw to
	 * @param loc  The location where to draw
	 * @param nrof How many squares in the stack
	 * @param col  Which column
	 */
	private void drawBar(Graphics2D g2, Coord loc, int nrof, int col) {
		final int BAR_HEIGHT = 5;
		final int BAR_WIDTH = 5;
		final int BAR_DISPLACEMENT = 2;

		// draws a stack of squares next loc
		for (int i = 1; i <= nrof; i++) {
			if (i % 2 == 0) { // use different color for every other msg
				g2.setColor(msgColor1);
			} else {
				if (col > 1) {
					g2.setColor(msgColor3);
				} else {
					g2.setColor(msgColor2);
				}
			}

			g2.fillRect(scale(loc.getX() - BAR_DISPLACEMENT - (BAR_WIDTH * col)),
				scale(loc.getY() - BAR_DISPLACEMENT - i * BAR_HEIGHT),
				scale(BAR_WIDTH), scale(BAR_HEIGHT));
		}
	}

	public static void processTrails(boolean process) {
		processTrails = process;
	}

	public static void setIsTrailEnabled(boolean isTrailEnabled) {
		// Assuming isTrailEnabled is a static field controlling trail drawing
		NodeGraphic.isTrailEnabled = isTrailEnabled;
	}

	public static void initializeTrailConfiguration(boolean enableTrails, int maxLength, Color defaultColor, boolean fade) {
		if (isTrailConfigured) {
			throw new IllegalStateException("Trail configuration can only be set once.");
		}
		isTrailEnabled = enableTrails;
		trailMaxLength = maxLength;
		if (defaultColor != null) {
			defaultTrailColor = defaultColor;
		}
		isTrailFadeEnabled = fade;
		isTrailConfigured = true;
	}

	private void updateAndDrawTrail(Graphics2D g2) {
		if (this.positionHistory == null) {
			return;
		}

		Coord currentLocation = node.getLocation();

		if (positionHistory.isEmpty() || !positionHistory.getLast().equals(currentLocation)) {
			positionHistory.addLast(currentLocation.clone());
		}

		if (isTrailFadeEnabled) {
			while (positionHistory.size() > trailMaxLength) {
				positionHistory.removeFirst();
			}
		}

		if (!isTrailEnabled || positionHistory.size() < 2) {
			return;
		}

		Coord prevPoint = null;
		int historySize = positionHistory.size();
		int pointIndex = 0;

		for (Coord currentPoint : positionHistory) {
			// only draw the trail if the node has a previous point and trail drawing is enabled
			if (prevPoint != null && isTrailEnabled) {
				Color segmentColor;

				if (isTrailFadeEnabled) {
					float alpha = (float) pointIndex / (float) (Math.max(1, historySize - 1));
					alpha = Math.max(0.1f, alpha);
					alpha = Math.min(1.0f, alpha);

					segmentColor = new Color(
						this.trailColor.getRed() / 255f,
						this.trailColor.getGreen() / 255f,
						this.trailColor.getBlue() / 255f,
						alpha);
				} else {
					segmentColor = this.trailColor;
				}

				g2.setColor(segmentColor);
				g2.drawLine(
					scale(prevPoint.getX()), scale(prevPoint.getY()),
					scale(currentPoint.getX()), scale(currentPoint.getY())
				);
			}
			prevPoint = currentPoint;
			pointIndex++;
		}
	}

	public static Color generateRandomColor() {
		Random rand = new Random();
		int r = rand.nextInt(256);
		int g = rand.nextInt(256);
		int b = rand.nextInt(256);
		return new Color(r, g, b);
	}

	/**
	 * Stores one frame of a GIF and its delay.
	 */
	private static class GIFFrame {

		BufferedImage image;
		int delay;

		GIFFrame(BufferedImage image, int delay) {
			this.image = image;
			this.delay = delay;
		}
	}
}

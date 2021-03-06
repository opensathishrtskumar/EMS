package com.ems.UI.custom.components;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class JMarqueeLabel extends JPanel implements Runnable {
	private static final long serialVersionUID = -2973353417536204185L;
	private int x;
	private FontMetrics fontMetrics;
	public static final int MAX_SPEED = 30;
	public static final int MIN_SPEED = 1;
	private int speed;
	public static final int SCROLL_TO_LEFT = 0;
	public static final int SCROLL_TO_RIGHT = 1;
	private int scrollDirection = 0;
	private boolean started = false;
	private JLabel label;

	public JMarqueeLabel(String text) {
		super();
		label = new JLabel(text) {
			private static final long serialVersionUID = -870580607070467359L;
			@Override
			protected void paintComponent(Graphics g) {
				g.translate(x, 0);
				super.paintComponent(g);
			}
		};
		setLayout(null);
		label.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 18));
		add(label);
		setSpeed(10);
		setScrollDirection(SCROLL_TO_RIGHT);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		label.paintComponents(g);
	}

	public void setScrollDirection(int scrollDirection) {
		this.scrollDirection = scrollDirection;
	}

	public int getScrollDirection() {
		return scrollDirection;
	}

	public void setSpeed(int speed) {
		if (speed < MIN_SPEED || speed > MAX_SPEED) {
			throw new IllegalArgumentException("speed out of range");
		}
		this.speed = speed;
	}

	public int getSpeed() {
		return speed;
	}

	@Override
	public void validateTree() {
		super.validateTree();
		label.setBounds(0, 0, 2000, getHeight());
		if (!started) {
			x = getWidth() + 10;
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
			started = true;
		}
	}

	public String getText() {
		return label.getText();
	}

	public void setText(String text) {
		label.setText(text);
	}

	public void setTextFont(Font font) {
		label.setFont(font);
		fontMetrics = label.getFontMetrics(label.getFont());
	}

	@Override
	public void run() {
		fontMetrics = label.getFontMetrics(label.getFont());
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		while (true) {
			if (scrollDirection == SCROLL_TO_LEFT) {
				x--;
				if (x < -fontMetrics.stringWidth(label.getText()) - 10) {
					x = getWidth() + 10;
				}
			}
			if (scrollDirection == SCROLL_TO_RIGHT) {
				x++;
				if (x > getWidth() + 10) {
					x = -fontMetrics.stringWidth(label.getText()) - 10;
				}
			}
			repaint();
			try {
				Thread.sleep(35 - speed);
			} catch (Exception e) {
			}
		}
	}
}

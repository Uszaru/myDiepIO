package uszaruStudio.myDiepIO.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.GrayFilter;
import javax.swing.JFrame;

import uszaruStudio.myDiepIO.entities.Enemy;
import uszaruStudio.myDiepIO.entities.Entity;
import uszaruStudio.myDiepIO.entities.Player;

public class MainEngine extends Canvas
		implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	private static final long serialVersionUID = -873356888766551529L;
	public final static int WIDTH = 854;
	public final static int HEIGHT = 480;
	public static int SCALE = 2;
	private Thread thr;
	private boolean isRunning = false;
	private int mapSizeIndex = 3;
	private boolean reviveEnemies;
	private boolean firstSpawn = true;
	private boolean isWaveMode = true;
	public static int mapSize;

	private static String[] menuItems;
	public static boolean down, up, enter, left, rigth;
	public static byte menuState;
	public static BufferedImage image;
	public static JFrame frame;
	public static Player player;
	private static MainEngine main;
	public static Camera cam;
	public static String gameState = "SELECT";
	public static int amountOfEnemies = 3;
	public static boolean areEnemiesSmart = true;
	public static int currentWave = 1, waveBefore = 0;
	public static boolean didWaveStart = false;

	public static List<Entity> entityList;
	public static Random rnd;
	public static int amountOfEnemiesAlive = 0;

	public MainEngine() {
		setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		initFrame();
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		System.out.println("Log: Loading game assets");

		loadGameAssets();

		addMouseListener(this);
		addKeyListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}

	private void loadGameAssets() {
		// TODO game assets here
		entityList = new ArrayList<>();
		rnd = new Random();
		player = new Player(64, 64);
		entityList.add(player);
		cam = new Camera();
		waveBefore = 0;
		didWaveStart = false;
		currentWave = 1;
		firstSpawn = true;
	}

	public static BufferedImage toGrayScale(BufferedImage colorImage) {
		ImageFilter filter = new GrayFilter(true, 20);
		ImageProducer producer = new FilteredImageSource(colorImage.getSource(), filter);
		Image image = Toolkit.getDefaultToolkit().createImage(producer);
		return toBufferedImage(image);
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	public static void main(String[] args) {
		System.out.println("Log: creating MainEngine");
		main = new MainEngine();
		main.start();
	}

	public synchronized void start() {
		System.out.println("Log: Starting Thread");
		thr = new Thread(this);
		isRunning = true;
		thr.start();
	}

	private void initFrame() {
		frame = new JFrame("Diep.io game c:");
		frame.add(this);
		frame.setResizable(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void tick() {
		switch (mapSizeIndex) {
		case 1:
			mapSize = 512;
			break;

		case 2:
			mapSize = 720;
			break;

		case 3:
			mapSize = 1028;
			break;

		case 4:
			mapSize = 2048;
			break;

		case 5:
			mapSize = 4096;
			break;

		default:
			mapSizeIndex = 3;
			break;
		}

		int numberOfEnemies = 0;

		for (int i = 0; i < entityList.size(); i++) {
			if (entityList.get(i) instanceof Enemy)
				numberOfEnemies++;
		}

		amountOfEnemiesAlive = numberOfEnemies;

		// player.typeCode = 1;

		if (gameState.equals("SELECT")) {
			menuItems = new String[6];
			menuItems[0] = "ENEMIES: " + amountOfEnemies;
			menuItems[1] = areEnemiesSmart ? "ENEMIES AI: SMART" : "ENEMIES AI: DUMB";
			menuItems[2] = "MAP SIZE:";
			menuItems[3] = reviveEnemies ? "RESPAWN ENEMIES: YES" : "RESPAWN ENEMIES: NO";
			menuItems[4] = isWaveMode ? "WAVE MODE" : "NORMAL MODE";
			menuItems[5] = "PLAY";

			switch (mapSizeIndex) {
			case 1:
				menuItems[2] += " TINY";
				break;

			case 2:
				menuItems[2] += " SMALL";
				break;

			case 3:
				menuItems[2] += " MEDIUM";
				break;

			case 4:
				menuItems[2] += " BIG";
				break;

			case 5:
				menuItems[2] += " HUGE";
				break;

			default:
				break;
			}

			if (down) {
				down = false;
				menuState++;
			} else if (up) {
				up = false;
				menuState--;
			}
			if (menuState > menuItems.length)
				menuState = 0;
			if (menuState < 0)
				menuState = (byte) menuItems.length;

			if (enter) {
				enter = false;
				if (menuState == 0) {
					amountOfEnemies++;
				} else if (menuState == 1) {
					if (areEnemiesSmart)
						areEnemiesSmart = false;
					else
						areEnemiesSmart = true;
				} else if (menuState == 2) {
					mapSizeIndex++;
					if (mapSizeIndex > 5)
						mapSizeIndex = 1;
				} else if (menuState == 3) {
					if (reviveEnemies)
						reviveEnemies = false;
					else
						reviveEnemies = true;
				} else if (menuState == 4) {
					if (isWaveMode)
						isWaveMode = false;
					else
						isWaveMode = true;
				} else if (menuState == 5) {
					restartGame();
				}
			}

			if (amountOfEnemies > 500)
				amountOfEnemies = 1;
			if (amountOfEnemies < 1)
				amountOfEnemies = 500;

			if (left) {
				left = false;
				if (menuState == 0)
					amountOfEnemies--;
				if (menuState == 2)
					mapSizeIndex--;
				if (mapSizeIndex < 1)
					mapSizeIndex = 5;
			} else if (rigth) {
				rigth = false;
				if (menuState == 0)
					amountOfEnemies++;
				if (menuState == 2)
					mapSizeIndex++;
				if (mapSizeIndex > 5)
					mapSizeIndex = 1;
			}
		} else if (gameState.equals("NORMAL")) {

			for (int i = 0; i < entityList.size(); i++) {
				entityList.get(i).tick();
			}
			if (!isWaveMode) {
				if (firstSpawn) {
					firstSpawn = false;
					for (int i = 0; i < amountOfEnemies; i++) {
						entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 0));
					}
				}
				if (numberOfEnemies < amountOfEnemies && reviveEnemies)
					entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 0));
			} else {

				// System.out.println("number:"+numberOfEnemies);

				if (numberOfEnemies == 0 && didWaveStart == true) {
					System.out.println("Game log: incriesing wave number, wave: " + currentWave);
					currentWave++;
				}

				// System.out.println("cur:"+currentWave+", before:"+waveBefore);

				if (currentWave > waveBefore) {
					didWaveStart = false;
					waveBefore = currentWave;
				}
				// currentWave = 1;

				if (didWaveStart == false) {
					didWaveStart = true;
					System.out.println("Game log: creating new wave enemies, cur wave: " + currentWave);
					if (currentWave == 1) {
						for (int i = 0; i < 3; i++) {
							entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 0));
						}
						mapSizeIndex = 2;
					} else if (currentWave == 2) {
						for (int i = 0; i < 5; i++) {
							entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 0));
						}
						mapSizeIndex = 3;
					} else if (currentWave == 3) {
						for (int i = 0; i < 8; i++) {
							entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 0));
						}
						for (int i = 0; i < 3; i++) {
							entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 1));
						}
						//entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 2));
						
						mapSizeIndex = 4;
					} else if (currentWave == 4) {
						for (int i = 0; i < 12; i++) {
							entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 0));
						}
						for (int i = 0; i < 5; i++) {
							entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 1));
						}
						entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 2));
					} else if (currentWave == 5) {
						for (int i = 0; i < 16; i++) {
							entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 0));
						}
						for (int i = 0; i < 5; i++) {
							entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 1));
							entityList.add(new Enemy(rnd.nextInt(mapSize), rnd.nextInt(mapSize), 2));
						}
					}
					// currentWave++;
				}
			}

		}
	}

	private void restartGame() {
		// TODO Auto-generated method stub
		loadGameAssets();
		gameState = "NORMAL";
	}

	private void render() {
		// TODO Auto-generated method stub
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			System.out.println("Log: creating buffer strategy");
			this.createBufferStrategy(3);
			return;
		}
		Graphics2D g = (Graphics2D) image.getGraphics();

		g.setColor(new Color(200, 200, 200));
		g.fillRect(0, 0, WIDTH * SCALE, HEIGHT * SCALE);

		int tempx1 = cam.x - ((cam.x / 64) * 64);
		int tempy1 = cam.y - ((cam.y / 64) * 64);

		int offsetX = tempx1 - tempx1 * 2, offsetY = tempy1 - tempy1 * 2;
		for (int i = -5; i < WIDTH / 60; i++) {
			for (int j = -5; j < WIDTH / 60; j++) {
				g.setColor(new Color(150, 150, 150));
				g.setStroke(new java.awt.BasicStroke(3));
				g.drawRect(i * 64 + offsetX, j * 64 + offsetY, 64, 64);
			}
		}

		if (gameState.equals("SELECT")) {
			for (int i = 0; i < menuItems.length; i++) {
				g.setColor(Color.black);
				if (menuState == i)
					g.setColor(Color.red);
				g.setFont(new Font(Font.DIALOG, Font.BOLD, 35));
				g.drawString(menuItems[i], MainEngine.WIDTH / 4, 50 + i * 50);
			}
		} else if (gameState.equals("NORMAL")) {
			for (int i = 0; i < entityList.size(); i++) {
				entityList.get(i).render(g);
			}

			g.setColor(new Color(255, 0, 0, 140));
			g.setStroke(new java.awt.BasicStroke(50));
			g.drawRect(0 - cam.x, 0 - cam.y, mapSize, mapSize);

			UIrender(g);
		}

		g.dispose();
		Graphics g1 = bs.getDrawGraphics();
		g1 = bs.getDrawGraphics();
		g1.drawImage(image, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);
		bs.show();
	}

	private void UIrender(Graphics2D g) {
		g.setColor(Color.black);
		g.fillRect(10, 10, MainEngine.WIDTH - 20, 50);

		g.setColor(Color.white);
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
		g.drawString("Enemies left: " + amountOfEnemiesAlive, 15, 28);

		g.setColor(Color.white);
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
		g.drawString("Current Wave: " + currentWave, 15, 48);

		if (player.isLVL1Unlocked && !player.isLVL1Chosen) {
			g.setColor(Color.white);
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
			g.drawString("Type 1 to choose machine-gun", 200, 28);

			g.setColor(Color.white);
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
			g.drawString("Type 2 to choose sniper", 200, 48);
		}
	}

	@Override
	public void run() {

		long lastTime = System.nanoTime();
		double amountOfTicks = 60;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		requestFocus();
		System.out.println("Log: starting main loop");
		while (isRunning) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			if (delta >= 1) {
				tick();
				render();
				delta = 0;
			}

		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		Player.mx = arg0.getX();
		Player.my = arg0.getY();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		Player.mx = arg0.getX();
		Player.my = arg0.getY();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		player.isHoldingShoot = true;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		player.isHoldingShoot = false;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_W) {
			player.up = true;
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			player.down = true;
		}

		if (e.getKeyCode() == KeyEvent.VK_A) {
			player.left = true;
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			player.rigth = true;
		}

		if (gameState.equals("SELECT")) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				enter = true;
			} else if (e.getKeyCode() == KeyEvent.VK_W) {
				up = true;
			} else if (e.getKeyCode() == KeyEvent.VK_S) {
				down = true;
			} else if (e.getKeyCode() == KeyEvent.VK_A) {
				left = true;
			} else if (e.getKeyCode() == KeyEvent.VK_D) {
				rigth = true;
			}
		}

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			gameState = "SELECT";
		}

		if (player.isLVL1Unlocked && !player.isLVL1Chosen) {
			if (e.getKeyCode() == KeyEvent.VK_1)
				player.typeToChangeTo = 1;
			if (e.getKeyCode() == KeyEvent.VK_2)
				player.typeToChangeTo = 2;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_W) {
			player.up = false;
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			player.down = false;
		}

		if (e.getKeyCode() == KeyEvent.VK_A) {
			player.left = false;
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			player.rigth = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
package uszaruStudio.myDiepIO.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import uszaruStudio.myDiepIO.main.MainEngine;

public class Player extends Entity {

	public Player(int x, int y) {
		super(x, y);
		// TODO Auto-generated constructor stub
		code = PLAYER_FORCE_CODE;
	}

	public static int mx, my;
	public boolean left, rigth, up, down, rotateLeft, rotateRigth;
	public double speed = 3;
	public int size = 30;
	public static boolean shoot;
	private double barrelAngle = 0; 

	public boolean isHoldingShoot, firerateInc;
	private int shootFirerateIndex, shootMaxFirerate = 15;
	int exp = 0;
	public long code;
	/**
	 * 0 = normal tank
	 * 1 = machinegun
	 * 2 = sniper
	 */
	public byte typeCode = 0, typeToChangeTo = 0;
	public boolean isLVL1Unlocked, isLVL1Chosen;
	private double life = 50, maxLife = 50, regenerateRate = 0.05;
	private int damage = 5;
	private double maxAngleCand = 20;

	@Override
	public void tick() {
		if(typeCode == 0) {
			speed = 3;
			maxLife = 50;
			regenerateRate = 0.05;
			damage = 5;
			shootMaxFirerate = 15;
			maxAngleCand  = 20;
		} else if(typeCode == 1) {
			speed = 2.5;
			maxLife = 70;
			regenerateRate = 0.05;
			damage = 6;
			shootMaxFirerate = 8;
			maxAngleCand  = 30;
		} else if(typeCode == 2) { 
			speed = 3;
			maxLife = 100;
			regenerateRate = 0.05;
			damage = 17;
			shootMaxFirerate = 30;
			maxAngleCand  = 7.5;
		}
		
		if(exp > 20)
			isLVL1Unlocked = true;  
		
		if(isLVL1Unlocked && !isLVL1Chosen) {
			if(typeToChangeTo != 0) {
				typeCode = typeToChangeTo;
				isLVL1Chosen = true;
				System.out.println("Game log: uptading type code to "+typeCode);
			}
		}
		
		MainEngine.cam.x = (int) this.x - MainEngine.WIDTH / 2;
		MainEngine.cam.y = (int) this.y - MainEngine.HEIGHT / 2;
		if (life < maxLife)
			life += regenerateRate;
		if (left) {
			this.x -= speed;
		} else if (rigth) {
			this.x += speed;
		}
		
		//typeCode = 2;

		if (up) {
			this.y -= speed;
		} else if (down) {
			this.y += speed;
		}
		
		if(x >  MainEngine.mapSize)
			x = MainEngine.mapSize;
		if(x < 0)
			x = 0;
		if(y >  MainEngine.mapSize)
			y =  MainEngine.mapSize;
		if(y < 0)
			y = 0;

		if (isHoldingShoot) {
			firerateInc = true;
		}

		if (shootFirerateIndex == 0 && isHoldingShoot)
			shoot = true;

		if (firerateInc)
			shootFirerateIndex++;
		if (shootFirerateIndex > shootMaxFirerate) {
			shootFirerateIndex = 0;
			firerateInc = false;
		}

		barrelAngle = Math.atan2(my / MainEngine.SCALE - (y - MainEngine.cam.y),
				mx / MainEngine.SCALE - (x - MainEngine.cam.x));
		// barrelAngle -= barrelAngle*2;
		if (shoot) {
			shoot = false;
			double angleToAdd = MainEngine.rnd.nextDouble()*maxAngleCand;
			angleToAdd -= maxAngleCand/2;
			angleToAdd = Math.toRadians(angleToAdd);
			double dx = Math.cos(barrelAngle+angleToAdd);
			double dy = Math.sin(barrelAngle+angleToAdd);

			dx -= dx * 2;
			dy -= dy * 2;

			Bullet bullet = new Bullet(x, y, dx, dy, 32, this.code, damage);
			MainEngine.entityList.add(bullet);
		}
		boolean didBulletHit = false;
		Bullet bulletHit = null;

		for (int i = 0; i < MainEngine.entityList.size(); i++) {
			if (MainEngine.entityList.get(i) instanceof Bullet) {
				Bullet bullet = (Bullet) MainEngine.entityList.get(i);
				if (distanceFromEntity(this.getX(), this.getY(), bullet.getX(), bullet.getY()) < this.size) {
					if (bullet.code != this.code) {
						didBulletHit = true;
						bulletHit = bullet;
					}
				}

			}
		}

		if (didBulletHit && bulletHit != null) {
			this.life -= bulletHit.damage;
			if (this.life < 1) {
				playerDeath();
			}
			MainEngine.entityList.remove(bulletHit);
		}
	}

	private void playerDeath() {
		// TODO Auto-generated method stub
		MainEngine.gameState = "SELECT";
	}

	@Override
	public void render(Graphics g) {

		Graphics2D g1 = (Graphics2D) g;
		

		g1.setColor(Color.gray);
		double d1, d2, d3;
		d1 = barrelAngle;
		d2 = x + (size / 2) - MainEngine.cam.x;
		d3 = y + (size / 2) - MainEngine.cam.y;
		g1.rotate(d1, d2, d3);
		if(typeCode == 0) {
			Rectangle rect2 = new Rectangle(getX() + (size / 4) - MainEngine.cam.x, getY() + (size / 4) - MainEngine.cam.y,
					50, 15);
			g1.fill(rect2);
		} else if(typeCode == 1) {
			Rectangle rect2 = new Rectangle(getX() + (size / 4) - MainEngine.cam.x, getY() + (size / 4) - MainEngine.cam.y,
					50, 20);
			//Polygon pol = Polygon.
			g1.fill(rect2);
			
			Rectangle rect3 = new Rectangle(getX() + (size / 4) - MainEngine.cam.x +40, getY() + (size / 4) - MainEngine.cam.y - 5,
					20, 30);
			//Polygon pol = Polygon.
			g1.fill(rect3);
		} else if(typeCode == 2) {
			Rectangle rect2 = new Rectangle(getX() + (size / 4) - MainEngine.cam.x, getY() + (size / 4) - MainEngine.cam.y,
					100, 15);
			g1.fill(rect2);
		}
		g1.rotate(d1 - (d1 * 2), d2, d3);
		// g1.
		g.setColor(Color.white);
		g.fillOval(getX() - MainEngine.cam.x, getY() - MainEngine.cam.y, size, size);

		g.setColor(Color.white);
		g.fillRoundRect(this.getX() - MainEngine.cam.x, this.getY() - 15 - MainEngine.cam.y,
				this.size, 10, 5, 5);
		
		g.setColor(Color.green);
		g.fillRoundRect(this.getX() - MainEngine.cam.x, this.getY() - 15 - MainEngine.cam.y,
				(int) (this.life / this.maxLife * this.size), 10, 5, 5);
	}
}

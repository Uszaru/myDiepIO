package uszaruStudio.myDiepIO.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import uszaruStudio.myDiepIO.main.MainEngine;

public class Enemy extends Entity {
	public double speed = 2;
	public int size = 32, shootIndex = 0, shootMaxFirerate = 15;
	private double barrelAngle;
	public long code;
	private double life = 50, maxLife = 50, regenerateRate = 0.01;
	private boolean knowsPlayer = false;
	private byte movementTime, maxMovementTime = 60;
	private boolean goUp, goDown, goLeft, goRigth;
	private int damage = 3;
	private double maxAngleCand = 15;
	private int typeCode = 0;

	public Enemy(double x, double y, int typeCode) {
		super(x, y);
		code = ENEMY_FORCE_CODE;
		this.typeCode = typeCode;
	}

	public int distanceFromPlayer() {
		return (int) distanceFromEntity(getX(), getY(), MainEngine.player.getX(), MainEngine.player.getY());
	}

	@Override
	public void tick() {
		if (x > MainEngine.mapSize)
			x = MainEngine.mapSize;
		if (x < 0)
			x = 0;
		if (y > MainEngine.mapSize)
			y = MainEngine.mapSize;
		if (y < 0)
			y = 0;

		if (typeCode == 0) {
			speed = 2;
			maxLife = 30;
			regenerateRate = 0.01;
			damage = 3;
			shootMaxFirerate = 15;
			maxAngleCand = 20;
		} else if (typeCode == 1) {
			speed = 1.7;
			maxLife = 50;
			regenerateRate = 0.01;
			damage = 4;
			shootMaxFirerate = 8;
			maxAngleCand = 30;
		} else if (typeCode == 2) {
			speed = 2;
			maxLife = 70;
			regenerateRate = 0.01;
			damage = 10;
			shootMaxFirerate = 30;
			maxAngleCand = 7.5;
		}

		if (distanceFromPlayer() < 360 || (knowsPlayer && distanceFromPlayer() < 720)) {
			knowsPlayer = true;
			shootIndex++;
			if (shootIndex > shootMaxFirerate) {
				shootIndex = 0;
				double angleToAdd = MainEngine.rnd.nextDouble() * maxAngleCand;
				angleToAdd -= maxAngleCand / 2;
				angleToAdd = Math.toRadians(angleToAdd);
				double dx = Math.cos(barrelAngle + angleToAdd-Math.toRadians(180));
				double dy = Math.sin(barrelAngle + angleToAdd-Math.toRadians(180));
				Bullet bullet = new Bullet(this.getX(), this.getY(), dx, dy, 32, this.code, damage);
				MainEngine.entityList.add(bullet);
			}
		}

		if (distanceFromPlayer() > 120 && distanceFromPlayer() < 480 && knowsPlayer) {
			movementTime++;
			if (movementTime > maxMovementTime) {
				movementTime = 0;
				goDown = false;
				goUp = false;
				goLeft = false;
				goRigth = false;
				if (MainEngine.rnd.nextBoolean()) {
					if (MainEngine.rnd.nextBoolean()) {
						goUp = true;
					} else {
						goDown = true;
					}
				}
				if (MainEngine.rnd.nextBoolean()) {
					if (MainEngine.rnd.nextBoolean()) {
						goLeft = true;
					} else {
						goDown = true;
					}
				}
			}

			if (goLeft) {
				this.x -= speed;
			} else if (goRigth) {
				this.x += speed;
			}

			if (goUp) {
				this.y -= speed;
			} else if (goDown) {
				this.y += speed;
			}
		}

		if ((distanceFromPlayer() > 480 && distanceFromPlayer() < 1080) && knowsPlayer) {
			if (MainEngine.player.getX() > getX()) {
				this.x += speed;
			} else if (MainEngine.player.getX() < getX()) {
				this.x -= speed;
			}

			if (MainEngine.player.getY() > getY()) {
				this.y += speed;
			} else if (MainEngine.player.getY() < getY()) {
				this.y -= speed;
			}
		}

		if (distanceFromPlayer() < 120) {
			if (MainEngine.player.getX() > getX()) {
				this.x -= speed;
			} else if (MainEngine.player.getX() < getX()) {
				this.x += speed;
			}

			if (MainEngine.player.getY() > getY()) {
				this.y -= speed;
			} else if (MainEngine.player.getY() < getY()) {
				this.y += speed;
			}
		}

		if (life < maxLife)
			life += regenerateRate;

		barrelAngle = Math.atan2(MainEngine.player.x - x, MainEngine.player.y - y);
		barrelAngle -= barrelAngle * 2;
		barrelAngle += Math.toRadians(90);

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
				MainEngine.entityList.remove(this);
				MainEngine.player.exp += 5;
			}
			MainEngine.entityList.remove(bulletHit);
			knowsPlayer = true;
		}
	}

	@Override
	public void render(Graphics g) {
		Graphics2D g1 = (Graphics2D) g;
		Rectangle rect2 = new Rectangle(getX() + (size / 4) - MainEngine.cam.x, getY() + (size / 4) - MainEngine.cam.y,
				50, 15);

		g1.setColor(Color.gray);
		double d1, d2, d3;
		d1 = barrelAngle;
		d2 = x + (size / 2) - MainEngine.cam.x;
		d3 = y + (size / 2) - MainEngine.cam.y;
		g1.rotate(d1, d2, d3);
		g1.fill(rect2);
		g1.rotate(d1 - (d1 * 2), d2, d3);
		// g1.
		g.setColor(Color.red);
		g.fillOval(getX() - MainEngine.cam.x, getY() - MainEngine.cam.y, size, size);

		g.setColor(Color.white);
		g.fillRoundRect(this.getX() - MainEngine.cam.x, this.getY() - 15 - MainEngine.cam.y, this.size, 10, 5, 5);

		g.setColor(Color.green);
		g.fillRoundRect(this.getX() - MainEngine.cam.x, this.getY() - 15 - MainEngine.cam.y,
				(int) (this.life / this.maxLife * this.size), 10, 5, 5);
	}
}

package uszaruStudio.myDiepIO.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import uszaruStudio.myDiepIO.main.MainEngine;

public class Bullet extends Entity{

	private double xvel, yvel;
	private int size;
	private int life = 600;
	private int speed = 5;
	double angle;
	public long code;
	public int damage = 1;

	public Bullet(double x, double y, double xvel, double yvel, int size, long code, int damage) {
		super(x, y);
		// TODO Auto-generated constructor stub
		this.xvel = xvel;
		this.yvel = yvel;
		this.size = size;
		this.angle = Math.atan2(xvel, yvel);
		angle -= angle * 2;
		//angle += Math.toRadians(-90);
		this.code = code;
		this.damage = damage;
	}
	
	@Override
	public void tick() {
		this.x -= xvel*speed;
		this.y -= yvel*speed;
		life --;
		if(life < 0)
			destroyItself();
	}

	
	private void destroyItself() {
		// TODO Auto-generated method stub
		MainEngine.entityList.remove(this);
	}

	@Override
	public void render(Graphics g) {
		Graphics2D g1 = (Graphics2D) g;
		Rectangle rect2 = new Rectangle(getX() + (size / 4)  - MainEngine.cam.x, getY() + (size / 4)  - MainEngine.cam.y, 5, 10);

		g1.setColor(Color.gray);
		double d1, d2, d3;
		d1 = angle;
		d2 = x + (size / 2)  - MainEngine.cam.x;
		d3 = y + (size / 2) - MainEngine.cam.y;
		g1.rotate(d1, d2, d3);
		g1.fill(rect2);
		g1.rotate(d1-(d1*2), d2, d3);
	}
}

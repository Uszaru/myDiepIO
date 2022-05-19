package uszaruStudio.myDiepIO.entities;

import java.awt.Graphics;

public class Entity {
	protected double x, y;
	//private int size;
	public final static int PLAYER_FORCE_CODE = 0, ENEMY_FORCE_CODE = 1;
	public Entity(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public void tick() {
		
	}
	
	public void render(Graphics g) {
		// TODO Auto-generated method stub

	}
	
	public static double distanceFromEntity(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}
	
	public int getX() {
		return (int) x;
	}
	
	public int getY() {
		return (int) y;
	}
}

package cancerBattleSim.agents;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public abstract class Cell {
	protected ContinuousSpace<Object> space;
	protected Grid<Object> grid;
	protected double speed;
	protected double multiply_chance;
	protected Random random = new Random();
	
	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getMultiply_chance() {
		return multiply_chance;
	}

	public void setMultiply_chance(double multiply_chance) {
		this.multiply_chance = multiply_chance;
	}
	
	public abstract void step();
	protected abstract void move();
	protected abstract void defend();
	protected abstract void travel();
	protected abstract void arrive();
	protected abstract void multiply();
	@SuppressWarnings("unchecked")
	protected void die() {
		Context<Object> context = ContextUtils.getContext(this);
		context.remove(this);
	};
	
	protected void moveTowards(Object target) {
		if (target == null) {
			double x_distance = RandomHelper.nextIntFromTo(-1, 1) * this.speed,
					y_distance = RandomHelper.nextIntFromTo(-1, 1) * this.speed,
					z_distance = RandomHelper.nextIntFromTo(-1, 1) * this.speed;

			NdPoint newLocation = this.space.moveByDisplacement(this, x_distance, y_distance, z_distance);

			this.grid.moveTo(this, (int) newLocation.getX(), (int) newLocation.getY(), (int) newLocation.getZ());
		} else {
			NdPoint agent_position = this.space.getLocation(this);
			NdPoint target_position = this.space.getLocation(target);
			double[] angle = angleFor3DMovement(agent_position, target_position);
			agent_position = this.space.moveByVector(this, this.speed, angle);
			this.grid.moveTo(this, (int) agent_position.getX(), (int) agent_position.getY(), (int) agent_position.getZ());
		}
	}

	// PRIVATE
	private static double[] angleFor3DMovement(NdPoint pt1, NdPoint pt2) {
		double x = pt1.getX() - pt2.getX();
		double y = pt1.getY() - pt2.getY();
		double z = pt1.getZ() - pt2.getZ();

		return new double[] { Math.atan2(Math.sqrt(Math.pow(y, 2) + Math.pow(z, 2)), x),
				Math.atan2(Math.sqrt(Math.pow(z, 2) + Math.pow(x, 2)), y),
				Math.atan2(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)), z) };
	}
}

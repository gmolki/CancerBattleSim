package cancerBattleSim;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class NCell {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	private enum Mode { MOVE, MULTIPLY }
	private Mode state;
	private double speed, multiply_chance;
	private String pattern;
	private Random random;

	public NCell(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.state = Mode.MOVE;
		this.speed = 0.001;
		this.pattern = "0000 0011";
		this.multiply_chance = 0.001;
		this.random = new Random();
	}

	@ScheduledMethod(start = 1, interval = 1)
	private void step () {
		switch (this.state) {
		case MULTIPLY:
			multiply();
			break;
		case MOVE:
			move();
			break;
		default:
			System.out.println("Unexpected state");
			break;
		}
	}

	private void move () {
		System.out.println("Move");
		if (random.nextFloat() < multiply_chance) {
			this.state = Mode.MULTIPLY;
		}
		moveTowards(null);

	}

	private void moveTowards (Object target) {
		if (target == null) {
			double speed_splited = speed / 3,
					x_distance = RandomHelper.nextIntFromTo(-1, 1) * speed_splited,
					y_distance = RandomHelper.nextIntFromTo(-1, 1) * speed_splited,
					z_distance = RandomHelper.nextIntFromTo(-1, 1) * speed_splited;

			NdPoint newLocation = 
					space.moveByDisplacement(this, x_distance, y_distance, z_distance);

			grid.moveTo(this, 
					(int)newLocation.getX(),
					(int)newLocation.getY(),
					(int)newLocation.getZ());
		}
	}

	private void multiply () {
		System.out.println("Multiply");
		NdPoint location = space.getLocation(this);
		Context<Object> context = ContextUtils.getContext(this);
		NCell newNCell = new NCell(space, grid);
		context.add(newNCell);
		space.moveTo(newNCell,
				location.getX(),
				location.getY(),
				location.getZ());
		NdPoint newCCellLocation = space.getLocation(newNCell);
		grid.moveTo(newNCell,
				(int)newCCellLocation.getX(),
				(int)newCCellLocation.getY(),
				(int)newCCellLocation.getZ());
		state = Mode.MOVE;
	}


}

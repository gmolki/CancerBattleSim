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

	@ScheduledMethod(start = 1, interval = 1, shuffle=true)
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
		if (random.nextFloat() < multiply_chance) {
			this.state = Mode.MULTIPLY;
		}
		Global.moveTowards(this, null, speed, space, grid);

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

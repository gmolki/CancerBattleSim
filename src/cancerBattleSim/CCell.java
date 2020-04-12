/**
 * 
 */
package cancerBattleSim;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

/**
 * @author gerard.molina
 *
 */
public class CCell {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	private enum Mode { MULTIPLY, MOVE, DEFEND, TRAVEL, ARRIVE, DIE };
	private Mode state;
	private double speed, multiply_chance;
	private String pattern;
	private Random random;
	public CCell(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.state = Mode.MOVE;
		this.speed = 0.003;
		this.pattern = "0000 0011";
		this.multiply_chance = 0;//0.0006;
		this.random = new Random();
	}

	@ScheduledMethod(start = 1, interval = 1, shuffle=true)
	public void step() {
		switch (state) {
		case MULTIPLY:
			multiply();
			break;
		case MOVE:
			move();
			break;
		case DEFEND:
			defend();
			break;
		case TRAVEL:
			travel();
			break;
		case ARRIVE:
			arrive();
			break;
		case DIE:
			die();
			break;
		default:
			System.out.println("Unexpected state");
			break;
		}
	}

	private void multiply() {
		NdPoint location = space.getLocation(this);
		Context<Object> context = ContextUtils.getContext(this);
		CCell newCCell = new CCell(space, grid);
		context.add(newCCell);
		space.moveTo(newCCell,
				location.getX(),
				location.getY(),
				location.getZ());

		NdPoint newCCellLocation = space.getLocation(newCCell);

		grid.moveTo(newCCell, 
				(int) newCCellLocation.getX(), 
				(int) newCCellLocation.getY(), 
				(int) newCCellLocation.getZ());

		state = Mode.MOVE;
	}

	private void move() {
		if (random.nextFloat() < multiply_chance) {
			state = Mode.MULTIPLY;
		}
		Global.moveTowards(this, null, speed, space, grid);

	}

	private void defend() { }
	private void travel() { }
	private void arrive() { }
	private void die() { }

}

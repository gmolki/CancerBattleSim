package cancerBattleSim.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import utils.Global;

public class NKCell extends Cell {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	private enum Mode {
		MULTIPLY, MOVE, LEARN, ATTACK, DORMANT, WAKEUP, DIE
	}

	private Mode state = Mode.MOVE;
	private double speed;
	private double multiply_chance;
	private double kill_distance;
	private double lose_distance;
	private double kill_chance;
	private int nsteps_no_ccells = 0;
	private int nsteps_noccells_for_dormant = 2;
	private int nsteps_with_ccells = 0;
	private int nsteps_with_ccells_for_wakeup = 2;
	// private String pattern = "0000 0010";

	private CCell target_ccell = null;
	private List<CCell> neighbor_ccells = new ArrayList<CCell>();

	private Random random = new Random();

	public NKCell(ContinuousSpace<Object> space, Grid<Object> grid, double kill_chance, double kill_distance,
			double lose_distance, double speed, double multiply_chance) {
		this.space = space;
		this.grid = grid;
		setKill_chance(kill_chance);
		setKill_distance(kill_distance);
		setLose_distance(lose_distance);
		setSpeed(speed);
		setMultiply_chance(multiply_chance);
	}

	@ScheduledMethod(start = 1, interval = 1, shuffle = true)
	public void step() {
		updateCCellsWithinDistance(lose_distance);

		if (hasCCellsNear()) {
			nsteps_no_ccells = 0;
			nsteps_with_ccells += 1;
		} else {
			nsteps_no_ccells += 1;
			nsteps_with_ccells = 0;
		}

		if (state != Mode.DORMANT && nsteps_no_ccells > nsteps_noccells_for_dormant) {
			state = Mode.DORMANT;
			Global.dormants += 1;
		} else if (state == Mode.DORMANT && nsteps_with_ccells > nsteps_with_ccells_for_wakeup) {
			state = Mode.WAKEUP;
		}

		switch (state) {
		case MULTIPLY:
			multiply();
			break;
		case MOVE:
			move();
			break;
		case LEARN:
			learn();
			break;
		case ATTACK:
			attack();
			break;
		case DORMANT:
			dormant();
			break;
		case WAKEUP:
			wakeup();
			break;
		case DIE:
			die();
			break;
		default:
			System.out.println("Unexpected state");
			break;
		}
	}

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

	public double getKill_distance() {
		return kill_distance;
	}

	public void setKill_distance(double kill_distance) {
		this.kill_distance = kill_distance;
	}

	public double getLose_distance() {
		return lose_distance;
	}

	public void setLose_distance(double lose_distance) {
		this.lose_distance = lose_distance;
	}

	public double getKill_chance() {
		return kill_chance;
	}

	public void setKill_chance(double kill_chance) {
		this.kill_chance = kill_chance;
	}

	private void learn() {
		state = Mode.MOVE;
	}

	public void move() {
		target_ccell = null;
		if (random.nextFloat() < multiply_chance) {
			state = Mode.MULTIPLY;
		} else {
			if (hasCCellsNear() && willAttack()) {
				// Get one random neighbor CCell
				int randomIndex = random.nextInt(neighbor_ccells.size());
				target_ccell = neighbor_ccells.get(randomIndex);
				state = Mode.ATTACK;
				// TODO: Create link to target_ccell
			}
		}
		moveTowards(this, target_ccell, speed, space, grid);
	}

	@SuppressWarnings("unchecked")
	public void multiply() {
		NdPoint location = space.getLocation(this);
		Context<Object> context = ContextUtils.getContext(this);
		NKCell newNKell = new NKCell(space, grid, getKill_chance(), getKill_distance(), getLose_distance(), getSpeed(),
				getMultiply_chance());
		context.add(newNKell);

		space.moveTo(newNKell, location.getX(), location.getY(), location.getZ());
		NdPoint newNKellLocation = space.getLocation(newNKell);
		grid.moveTo(newNKell, (int) newNKellLocation.getX(), (int) newNKellLocation.getY(),
				(int) newNKellLocation.getZ());

		state = Mode.MOVE;
	}

	@SuppressWarnings("unchecked")
	private void attack() {
		if (target_ccell != null) {
			/*
			 * TODO: if no link with target_ccell state = move
			 */
			GridPoint position = grid.getLocation(this), target_position = grid.getLocation(target_ccell);
			if (target_position != null) {
				double distanceToTarget = grid.getDistance(position, target_position);

				if (canKill(distanceToTarget)) {
					// TODO: self-destroy NKCell with the CCell after X kill streak
					Context<Object> context = ContextUtils.getContext(this);
					context.remove(target_ccell);
				} else if (isInsideRadius(distanceToTarget)) {
					moveTowards(this, target_ccell, speed, space, grid);
				} else {
					// TODO: Destroy the link with the target_ccell
					target_ccell = null;
					state = Mode.MOVE;
				}
			} else {
				target_ccell = null;
			}
		}
	}

	private void dormant() {
		nsteps_with_ccells = 0;
		state = Mode.MOVE;
	}

	private void wakeup() {
		nsteps_no_ccells = 0;
		state = Mode.MOVE;
	}

	private boolean hasCCellsNear() {
		return neighbor_ccells.size() > 0;
	}

	private boolean willAttack() {
		return random.nextFloat() < (kill_chance * Global.KILL_CHANCE);
	}

	private boolean canKill(double distance) {
		return distance < kill_distance;
	}

	private boolean isInsideRadius(double distance) {
		return distance < lose_distance;
	}

	public void updateCCellsWithinDistance(double distance) {
		GridPoint position = grid.getLocation(this);
		double xPosCell = position.getX(), yPosCell = position.getY(), zPosCell = position.getZ();

		neighbor_ccells = new ArrayList<CCell>();

		for (double x = xPosCell - distance; x < xPosCell + distance; x += distance) {
			for (double y = yPosCell - distance; y < yPosCell + distance; y += distance) {
				for (double z = zPosCell - distance; z < zPosCell + distance; z += distance) {
					for (Object detectedCell : grid.getObjectsAt((int) x, (int) y, (int) z)) {
						if (detectedCell instanceof CCell) {
							neighbor_ccells.add((CCell) detectedCell);
						}
					}
				}
			}
		}
	}

	@Override
	protected void defend() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void travel() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void arrive() {
		// TODO Auto-generated method stub

	}
}

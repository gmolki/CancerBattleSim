package cancerBattleSim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.query.space.grid.GridWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class NKCell {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	private enum Mode { MULTIPLY, MOVE, LEARN, ATTACK,  DORMANT, WAKEUP }
	private Mode state;
	private double speed, multiply_chance, kill_distance, lose_distance, kill_chance,
	RESTING, IL15, NKG2D, MICA, ULBP2, HLAI;
	private int nsteps_no_ccells, nsteps_noccells_for_dormant,
	nsteps_with_ccells, nsteps_with_ccells_for_wakeup, born,
	nccells, RATIO;
	private String pattern;
	private CCell target_ccell; 
	private List<CCell> neighbor_ccells;
	
	private Random random;

	public NKCell(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.state = Mode.MOVE;
		this.speed = 0.01;
		this.multiply_chance = 0.0006;
		this.kill_distance = 0.5;
		this.lose_distance = 1.0;
		this.kill_chance = 1.0;
		this.nsteps_no_ccells = 0;
		this.nsteps_noccells_for_dormant = 2;
		this.nsteps_with_ccells = 0;
		this.nsteps_with_ccells_for_wakeup = 2;
		this.pattern = "0000 0010";
		this.born = 0;
		this.nccells = 0;
		/********EXPERIMENTAL CONTROL PARAMETERS*******/	
		this.RATIO = 1;
		this.RESTING = 0.0;
		this.IL15 = 0.0;
		this.NKG2D = 0.0;
		this.MICA = 0.0;
		this.ULBP2 = 0.0;
		this.HLAI = 1.0;
		/**********************************************/
		this.target_ccell = null;
		this.neighbor_ccells = new ArrayList<CCell>();
		this.random = new Random();
	}

	@ScheduledMethod(start = 1, interval = 1, shuffle=true)
	public void step() {
		updateCCellsWithinDistance(lose_distance);
		
		if (hasCCellsNear()) {
			nsteps_no_ccells -= 1;
			nsteps_with_ccells += 1;
		} else {
			nsteps_no_ccells += 1;
			nsteps_with_ccells -= 1;
		}
		
		if (state != Mode.DORMANT 
				&& nsteps_no_ccells > nsteps_noccells_for_dormant) {
			state = Mode.DORMANT;
			Global.dormants += 1;
		} else if (state == Mode.DORMANT 
				&& nsteps_with_ccells > nsteps_with_ccells_for_wakeup) {
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
		default:
			System.out.println("Unexpected state");
			break;
		}
	}

	private void learn() {
		state = Mode.MOVE;
	}

	private void move() {
		target_ccell = null;
		if (random.nextFloat() < multiply_chance) {
			state = Mode.MULTIPLY;
		} else {
			GridPoint location = grid.getLocation(this);

			if(hasCCellsNear() && (random.nextFloat() < (kill_chance*Global.KILL_CHANCE))) {
				// Get one random neighbor CCell
				int randomIndex = random.nextInt(neighbor_ccells.size());
				target_ccell = neighbor_ccells.get(randomIndex);
				state = Mode.ATTACK;
				//TODO: Create link to target_ccell
			}
		}
		Global.moveTowards(this, target_ccell, speed, space, grid);
	}

	private void multiply() {
		NdPoint location = space.getLocation(this);
		Context<Object> context = ContextUtils.getContext(this);
		NKCell newNKell = new NKCell(space, grid);
		context.add(newNKell);
		
		space.moveTo(newNKell,
				location.getX(),
				location.getY(),
				location.getZ());
		NdPoint newNKellLocation = space.getLocation(newNKell);
		grid.moveTo(newNKell, 
				(int) newNKellLocation.getX(), 
				(int) newNKellLocation.getY(), 
				(int) newNKellLocation.getZ());

		state = Mode.MOVE;
	}

	private void attack() {
		if(target_ccell != null) {
			/*TODO:
			if no link with target_ccell
				state = move*/
			GridPoint position = grid.getLocation(this),
					target_position = grid.getLocation(target_ccell);
			if(target_position != null) {
				double distanceToTarget = grid.getDistance(position, target_position);
				
				if (canKill(distanceToTarget)) {
					//TODO: autodestruct nkcell with the ccell
					Context<Object> context = ContextUtils.getContext(this);
					context.remove(target_ccell);
					context.remove(this);
				}
				else if(isInsideRadius(distanceToTarget)) {
					Global.moveTowards(this, target_ccell, speed, space, grid);
				}else {
					//TODO: Destroy the link with the target_ccell
					target_ccell = null;
					state = Mode.MOVE;
				}
			}else {
				target_ccell = null;
			}
		}
	}

	private boolean hasCCellsNear() {
		return neighbor_ccells.size() > 0;
	}
	
	private boolean canKill(double distance) {
		return distance < kill_distance;
	}
	
	private boolean isInsideRadius(double distance) {
		return distance < lose_distance;
	}

	private void dormant() {
		nsteps_with_ccells = 0;
		state = Mode.MOVE;
	}

	private void wakeup() {
		nsteps_no_ccells = 0;
		state = Mode.MOVE;
	}

	private void setExperiment() {
		//TODO
	}

	public void updateCCellsWithinDistance (double distance) {
		GridPoint position = grid.getLocation(this);
		double 	xPosCell = position.getX(),
				yPosCell = position.getY(),
				zPosCell = position.getZ();
		
		neighbor_ccells = new ArrayList<CCell>();
		
		for (double x = xPosCell - distance; x < xPosCell + distance; x += distance) {
			for (double y = yPosCell - distance; y < yPosCell + distance; y += distance) {
				for (double z = zPosCell - distance; z < zPosCell + distance; z += distance) {
					for(Object detectedCell : grid.getObjectsAt((int) x, (int) y, (int) z)) {
						if (detectedCell instanceof CCell) { 
							neighbor_ccells.add((CCell) detectedCell);
						}
					}
				}
			}
		}
	}
}

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
		this.RATIO = 1;
		this.RESTING = 0.0;
		this.IL15 = 0.0;
		this.NKG2D = 0.0;
		this.MICA = 0.0;
		this.ULBP2 = 0.0;
		this.HLAI = 1.0;
		this.target_ccell = null;
		this.random = new Random();
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
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
		state = Mode.MULTIPLY;
	}

	private void move() {
		if (random.nextFloat() < multiply_chance) {
			state = Mode.MULTIPLY;
		} else {
			System.out.println("move");
			GridPoint location = grid.getLocation(this);

//			getNeighborsWithinDistance(this, 50);
//			GridWithin<Object> neighbor_cells = new GridWithin<Object>(grid, this, lose_distance);

//			System.out.println(neighbor_cells.toString());
			//			
			//			// use the GridCellNgh class to create GridCells for the surrounding neighborhood
			//			GridCellNgh<CCell> nghCreator = new GridCellNgh<CCell>(grid, location, CCell.class, 1, 1, 1);
			//			List<GridCell<CCell>> gridCells = nghCreator.getNeighborhood(true);
			//			
			//			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
			//			
//			List<Object> ccells = new ArrayList<Object>();
//
//			// Adds all CCell neighbors to the list
//			for (Object cell : neighbor_cells.query()) {
//				System.out.println(cell.toString());
//				if (cell instanceof CCell) {
//					ccells.add(cell);
//				}
//			}
//
//			if(ccells.size() > 0) {
//
//			}

		}
	}

	private void multiply() {

	}

	private void attack() {

	}

	private void hasCcellsNear() {

	}

	private void dormant() {

	}

	private void wakeup() {

	}

	private void moveTowards() {

	}

	private void setExperiment() {

	}

	public List<Object> getNeighborsWithinDistance (NKCell nkCell, double distance) {
		GridPoint position = grid.getLocation(nkCell);
		double 	xPosCell = position.getX(),
				yPosCell = position.getY(),
				zPosCell = position.getZ();
		
		List<Object> neighbor_cells = new ArrayList<Object>();
		
		for (double x = xPosCell - distance; x < xPosCell + distance; x += distance) {
			for (double y = yPosCell - distance; y < yPosCell + distance; y += distance) {
				for (double z = zPosCell - distance; z < zPosCell + distance; z += distance) {
					for(Object detectedCell : grid.getObjectsAt((int) x, (int) y, (int) z)) {
						neighbor_cells.add(detectedCell);
					}
				}
			}
		}




		return null;
	}

}

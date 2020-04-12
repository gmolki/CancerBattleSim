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
		
		/****************SET EXPERIMENT****************/
		this.setExperiment();
		/**********************************************/
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
		double targetpercent = 0.0,
				target = 0.0,
				current_value = 0.0,
				diff = 0.0,
				diffpercent = 0.0;
		
		int nexperiments = 5,
				nparameters = 6;
		/*
		 * EXPERIMENT	RESTING	IL15	ULBP2	MICA	NKG2D	HLAI	RATIOS		TARGETS (per ratio)
		 * 1			1		0		0		0		0		0		1,2,4,8		0.14, 0.16, 0.19, 0.26
		 * 2			0		1		0		0		0		0					0.19, 0.27, 0.40, 0.45
		 * 3			0		0		1		1		1		0					0.10, 0.10, 0.11, 0.13
		 * 4			0		0		0		0		0		1					0.42, 0.55, 0.66, 0.73				
		 * 5			0		1		0		0		0		1					0.41, 0.57, 0.74, 0.87
		 */
		
		int[][] experiments = new int[nexperiments][nparameters];
		experiments[0][0]=1;experiments[0][1]=0;experiments[0][2]=0;experiments[0][3]=0;experiments[0][4]=0;experiments[0][5]=0;
		experiments[1][0]=0;experiments[1][1]=1;experiments[1][2]=0;experiments[1][3]=0;experiments[1][4]=0;experiments[1][5]=0;
		experiments[2][0]=0;experiments[2][1]=0;experiments[2][2]=1;experiments[2][3]=1;experiments[2][4]=1;experiments[2][5]=0;
		experiments[3][0]=0;experiments[3][1]=0;experiments[3][2]=0;experiments[3][3]=0;experiments[3][4]=0;experiments[3][5]=1;
		experiments[4][0]=0;experiments[4][1]=1;experiments[4][2]=0;experiments[4][3]=0;experiments[4][4]=0;experiments[4][5]=1;
		
		
		int experiment_id = 4;  // IMPORTANT. THIS CHOOSES THE EXPERIMENT ! goes from 0 to 4
		
		/***** EXPERIMENT 1 *****/									// 0.14, 0.16, 0.19, 0.26
		//1:1 0.36  gave 343 and should be 344 * (redone)
		//2:1 0.3 gave 335 and should be 336 * (redone)
		//4:1 0.25 gave 328 and should be 324 * (redone)
		//8:1 0.24 gave 286 and should be 296 * (redone)
		/***** EXPERIMENT 2 *****/									// 0.19, 0.27, 0.40, 0.45
		//1:1 0.4 gave 319 and should be 324 * (redone)
		//2:1 0.35 gave 286 and should be 293 * (redone)
		//4:1 0.3 gave 244 and should be 240 * (redone)
		//8:1 0.25 gave 214 and should be 220 * (redone)
		/***** EXPERIMENT 3 *****/ 									// 0.10, 0.10, 0.11, 0.13
		//1:1 0.72 gave 364 and should be 360 *
		//2:1 0.68 gave 367 and should be 360 *
		//4:1 0.65 gave 346 and should be 356 *
		//8:1 0.61 gave 346 and should be 348 *
		/***** EXPERIMENT 4 *****/ 									// 0.42, 0.55, 0.66, 0.73
		//1:1 0.56 gave 236 and should be 232 * (done)
		//2:1 0.50 gave 184 and should be 180 * (done)
		//4:1 0.445 gave 137 and should be 136 * (done)
		//8:1 0.345 gave 105 and should be 108 * (done)
		/***** EXPERIMENT 5 *****/ 									// 0.41, 0.57, 0.74, 0.87
		//1:1 0.73 gave 230 and should be 236 * (done)
		//2:1 0.685 gave 175 and should be 172 * (done)
		//4:1 0.643 gave 108 and should be 104 * (done)
		//8:1 0.62 gave 54 and should be 52 * (done)
		
		double[][] weights = new double[nparameters][nexperiments];
		weights[0][0]=1.0;weights[0][1]=1.0;weights[0][2]=1.0;weights[0][3]=1.0;weights[0][4]=1.0; 	// feature 1, RESTING
		weights[1][0]=1.1;weights[1][1]=1.1;weights[1][2]=1.1;weights[1][3]=1.1;weights[1][4]=1.1; 	// feature 2, IL15
		weights[2][0]=0.9;weights[2][1]=0.9;weights[2][2]=0.9;weights[2][3]=0.9;weights[2][4]=0.9;  // feature 3, ULBP2
		weights[3][0]=0.9;weights[3][1]=0.9;weights[3][2]=0.9;weights[3][3]=0.9;weights[3][4]=0.9; 	// feature 4, MICA
		weights[4][0]=0.9;weights[4][1]=0.9;weights[4][2]=0.9;weights[4][3]=0.9;weights[4][4]=0.9; 	// feature 5, NKG2D
		weights[5][0]=1.1;weights[5][1]=1.1;weights[5][2]=1.1;weights[5][3]=1.1;weights[5][4]=1.1; 	// feature 6, HLAI
		
		for (int i = 0; i < nparameters; i++) {
			for (int j = 0; j < nexperiments; j++) {
				weights[i][j] = weights[i][j]*((Global.RATIO*0.62)/(Global.RATIO));
			}
		}
		
		if (experiments[experiment_id][0] == 1)  
		{
			kill_chance   	= kill_chance   	* weights[0][0];
			kill_distance 	= kill_distance 	* weights[0][1];
			lose_distance 	= lose_distance 	* weights[0][2];
			speed 			= speed 		  	* weights[0][3];
			multiply_chance 	= multiply_chance * weights[0][4];
		}
		if (experiments[experiment_id][1] == 1) 
		{
			kill_chance   	= kill_chance   	* weights[1][0];
			kill_distance 	= kill_distance 	* weights[1][1];
			lose_distance 	= lose_distance 	* weights[1][2];
			speed 			= speed 		  	* weights[1][3];
			multiply_chance 	= multiply_chance * weights[1][4];
		}
		if (experiments[experiment_id][2] == 1)
		{
			kill_chance   	= kill_chance   	* weights[2][0];
			kill_distance 	= kill_distance 	* weights[2][1];
			lose_distance 	= lose_distance 	* weights[2][2];
			speed 			= speed 		  	* weights[2][3];
			multiply_chance 	= multiply_chance * weights[2][4];
		}
		if (experiments[experiment_id][3] == 1)
		{
			kill_chance   	= kill_chance   	* weights[3][0];
			kill_distance 	= kill_distance 	* weights[3][1];
			lose_distance 	= lose_distance 	* weights[3][2];
			speed 			= speed 		  	* weights[3][3];
			multiply_chance 	= multiply_chance * weights[3][4];
		}
		if (experiments[experiment_id][4] == 1)
		{
			kill_chance   	= kill_chance   	* weights[4][0];
			kill_distance 	= kill_distance 	* weights[4][1];
			lose_distance 	= lose_distance 	* weights[4][2];
			speed 			= speed 		  	* weights[4][3];
			multiply_chance 	= multiply_chance * weights[4][4];
		}
		if (experiments[experiment_id][5] == 1)
		{
			kill_chance   	= kill_chance   	* weights[5][0];
			kill_distance 	= kill_distance 	* weights[5][1];
			lose_distance 	= lose_distance 	* weights[5][2];
			speed 			= speed 		  	* weights[5][3];
			multiply_chance 	= multiply_chance * weights[5][4];
		}
		
		
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

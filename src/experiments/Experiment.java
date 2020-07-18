package experiments;

import cells.CCell;
import cells.NCell;
import cells.NKCell;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import utils.Global;

public class Experiment {
	private Context<Object> context;
	private int xDim = 40;
	private int yDim = 40;
	private int zDim = 4;
	private int ncellCount = 0;
	private int nkcellCount = 0;
	private int ccellCount = 0;
	/******* CELL FEATURES *******/
	private final int KILL_CHANCE = 0;
	private final int KILL_DISTANCE = 1;
	private final int LOSE_DISTANCE = 2;
	private final int SPEED = 3;
	private final int MULTIPLY_CHANCE = 4;
	private final int NUM_CELL_FEATURES = 5;
	/*****************************/
	/***** CONTROL PARAMETERS ****/
	private final int RESTING = 0;
	private final int IL15 = 1;
	private final int ULBP2 = 2;
	private final int MICA = 3;
	private final int NKG2D = 4;
	private final int HLAI = 5;
	private final int NUM_CONTROL_PARAMETERS = 6;
	/*****************************/
	/****** NKCELL FEATURES ******/
	private double nk_cell_speed = 0.01;
	private double nk_cell_multiply_chance = 0.0006;
	private double nk_cell_kill_distance = 0.5;
	private double nk_cell_lose_distance = 1.0;
	private double nk_cell_kill_chance = 1.0;
	/*****************************/

	private double nk_cell_features[] = new double[NUM_CELL_FEATURES];
	private int control_parameters[] = new int[NUM_CONTROL_PARAMETERS];
	private double weights[] = new double[NUM_CONTROL_PARAMETERS]; // Describe how control parameters affect to cell
																	// features

	public Experiment(Context<Object> context, int resting, int il15, int ulbp2, int mica, int nkg2d, int hlai) {
		this.context = context;
		setControlParameters(resting, il15, ulbp2, mica, nkg2d, hlai);
		setNKCellFeatures();
		setWeights();
	}

	public Context<Object> setExperiment(ContinuousSpace<Object> space, Grid<Object> grid) {
		return createCellsForRatio(space, grid);
	}

	public int getxDim() {
		return xDim;
	}

	public int getyDim() {
		return yDim;
	}

	public int getzDim() {
		return zDim;
	}

	// PRIVATE
	private void setNKCellFeatures() {
		nk_cell_features[KILL_CHANCE] = nk_cell_kill_chance;
		nk_cell_features[KILL_DISTANCE] = nk_cell_kill_distance;
		nk_cell_features[LOSE_DISTANCE] = nk_cell_lose_distance;
		nk_cell_features[SPEED] = nk_cell_speed;
		nk_cell_features[MULTIPLY_CHANCE] = nk_cell_multiply_chance;
	}

	private void setControlParameters(int resting, int il15, int ulbp2, int mica, int nkg2d, int hlai) {
		control_parameters[RESTING] = resting;
		control_parameters[IL15] = il15;
		control_parameters[ULBP2] = ulbp2;
		control_parameters[MICA] = mica;
		control_parameters[NKG2D] = nkg2d;
		control_parameters[HLAI] = hlai;
	}

	private void setWeights() {
		weights[RESTING] = 1.0;
		weights[IL15] = 1.1;
		weights[ULBP2] = 0.9;
		weights[MICA] = 0.9;
		weights[NKG2D] = 0.9;
		weights[HLAI] = 1.1;
	}

	/**
	 * Creation of new Cells and adding it to the simulation space
	 * 
	 * @param context
	 * @return context with new Cells into it
	 */
	private Context<Object> createCellsForRatio(ContinuousSpace<Object> space, Grid<Object> grid) {
		Parameters params = RunEnvironment.getInstance().getParameters();
		Global.RATIO = params.getFloat("cells_ratio");

		calculateCellsForRatio();

		params.setValue("ccell_count", ccellCount);
		for (int i = 0; i < ccellCount; i++) {
			context.add(new CCell(space, grid));
		}

		params.setValue("ncell_count", ncellCount);
		for (int i = 0; i < ncellCount; i++) {
			context.add(new NCell(space, grid));
		}

		params.setValue("nkcell_count", nkcellCount);
		for (int i = 0; i < nkcellCount; i++) {
			context.add(new NKCell(space, grid, nk_cell_features[KILL_CHANCE], nk_cell_features[KILL_DISTANCE],
					nk_cell_features[LOSE_DISTANCE], nk_cell_features[SPEED], nk_cell_features[MULTIPLY_CHANCE]));
		}

		for (Object cell : context) {
			NdPoint pt = space.getLocation(cell);
			grid.moveTo(cell, (int) pt.getX(), (int) pt.getY(), (int) pt.getZ());
		}

		return context;
	}

	private void calculateCellsForRatio() {
		int volume = xDim * yDim * zDim;

		ccellCount = (int) (volume / (Global.RATIO + 1));
		nkcellCount = volume - ccellCount;
	}
}
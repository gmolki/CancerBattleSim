package cancerBattleSim.experiment;

import cancerBattleSim.agents.CCell;
import cancerBattleSim.agents.NKCell;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import utils.GlobalVariables;

public class Experiment {
	private Context<Object> context;
	Parameters experiment_params;
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
	private final int NUM_control_parameters = 6;
	/*****************************/
	/****** NKCELL FEATURES ******/
	private double nk_cell_speed = 0.05;
	private double nk_cell_multiply_chance = 0.0006;
	private double nk_cell_kill_distance = 0.25;
	private double nk_cell_lose_distance = 0.5;
	private double nk_cell_kill_chance = 0.5;
	/*****************************/

	private double nk_cell_features[] = new double[NUM_CELL_FEATURES];
	private boolean control_parameters_activated[] = new boolean[NUM_control_parameters];
	private double weights[] = new double[NUM_control_parameters]; // Describe how control parameters affect to cell
																	// feature

	public Experiment(Context<Object> context, boolean resting, boolean il15, boolean ulbp2, boolean mica,
			boolean nkg2d, boolean hlai) {
		this.context = context;
		experiment_params = RunEnvironment.getInstance().getParameters();
		setControlParameters(resting, il15, ulbp2, mica, nkg2d, hlai);
		setWeights();
		setNKCellFeatures();
	}

	public int getCCellCount() {
		return ccellCount;
	}

	public int getNKCellCount() {
		return nkcellCount;
	}

	public Context<Object> setExperiment(ContinuousSpace<Object> space, Grid<Object> grid) {
		setNKCellWeightedFeaturesValues();
		return createCellsForRatio(space, grid);
	}

	// PRIVATE
	private void setNKCellFeatures() {
		nk_cell_features[KILL_CHANCE] = nk_cell_kill_chance;
		nk_cell_features[KILL_DISTANCE] = nk_cell_kill_distance;
		nk_cell_features[LOSE_DISTANCE] = nk_cell_lose_distance;
		nk_cell_features[SPEED] = nk_cell_speed;
		nk_cell_features[MULTIPLY_CHANCE] = nk_cell_multiply_chance;
	}

	private void setControlParameters(boolean resting, boolean il15, boolean ulbp2, boolean mica, boolean nkg2d,
			boolean hlai) {
		control_parameters_activated[RESTING] = resting;
		control_parameters_activated[IL15] = il15;
		control_parameters_activated[ULBP2] = ulbp2;
		control_parameters_activated[MICA] = mica;
		control_parameters_activated[NKG2D] = nkg2d;
		control_parameters_activated[HLAI] = hlai;
	}

	private void setWeights() {
		weights[RESTING] = experiment_params.getDouble("resting") * 0.3;
		weights[IL15] = experiment_params.getDouble("il15") * 3.0;
		weights[ULBP2] = experiment_params.getDouble("ulbp2");
		weights[MICA] = experiment_params.getDouble("mica");
		weights[NKG2D] = experiment_params.getDouble("nkg2d");
		weights[HLAI] = experiment_params.getDouble("hlai") * 3.0;
	}

	private void setNKCellWeightedFeaturesValues() {
		for (int i = 0; i < NUM_control_parameters; i++) {
			if (control_parameters_activated[i] && weights[i] != 0) {
				nk_cell_features[KILL_CHANCE] = nk_cell_features[KILL_CHANCE] * weights[i];
				nk_cell_features[KILL_DISTANCE] = nk_cell_features[KILL_DISTANCE] * weights[i];
				nk_cell_features[LOSE_DISTANCE] = nk_cell_features[LOSE_DISTANCE] * weights[i];
				nk_cell_features[SPEED] = nk_cell_features[SPEED] * weights[i];
				nk_cell_features[MULTIPLY_CHANCE] = nk_cell_features[MULTIPLY_CHANCE] * weights[i];
			}
		}
	}

	/**
	 * Creation of new Cells and adding it to the simulation space
	 * 
	 * @param context
	 * @return context with new Cells into it
	 */
	private Context<Object> createCellsForRatio(ContinuousSpace<Object> space, Grid<Object> grid) {
		calculateCellsForRatio();

		for (int i = 0; i < ccellCount; i++) {
			context.add(new CCell(space, grid));
		}

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
		int volume = GlobalVariables.xDim * GlobalVariables.yDim * GlobalVariables.zDim;

		ccellCount = (int) (volume / (experiment_params.getInteger("cells_ratio") + 1));
		nkcellCount = volume - ccellCount;
	}
}
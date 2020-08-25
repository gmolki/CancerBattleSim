package automaticWeightCalculation.runs.controllers;

import java.util.ArrayList;
import java.util.List;

import automaticWeightCalculation.runs.Run;
import utils.Archiver;
import utils.GlobalVariables;

public class ExperimentRunsController {
	private List<Run> runs = new ArrayList<Run>(); // List containing all the runs that will be executed
	private int stable_tick = 1000;
	private BatchController batch_controller;

	// CONTROL PARAMETERS
	private final int RESTING = 0;
	private final int HLAI = 1;
	private final int ULBP2 = 2;
	private final int NKG2D = 3;
	private final int MICA = 4;
	private final int IL15 = 5;
	private boolean[] control_parameters_activation; // Array containing whether control parameters are activated or not

	private int ratio;
	private int expected_ccells;
	
	public ExperimentRunsController(int experiment, int ratio) {
		this.runValuesForExperiment(experiment, ratio);
		this.batch_controller = new BatchController(this.control_parameters_activation);
	}

	public Run generateParentRun() {
		return setUpRun(0.0, 0.0, 0.0, 0.0, 0.0, 0.0); // First run with all values set at 0.0
	}
	

	public boolean[] getControlParametersActivation() {
		return this.control_parameters_activation;
	}

	public int getExpectedCCells() {
		return this.expected_ccells;
	}
	
	public List<Run> getRuns(Run previous_best_run) {
		this.clearPreviousRuns();
		return this.generateNewRuns(previous_best_run);
	}

	public boolean nextSetOfRuns(boolean is_improving) {
		batch_controller.nextBatch(is_improving);
		return this.canContinue();
	}
	
	private void clearPreviousRuns() {
		runs.clear();
		Archiver.cleanRunsFolder();
	}
	
	private List<Run> generateNewRuns(Run previous_best_run) {
		
		int batch = batch_controller.getBatch();
		int weight = batch_controller.getParameterIndex();
		int decimal_division = batch_controller.getDecimalDivision();
		double[] control_parameters_values = new double[] { previous_best_run.getResting(),
				previous_best_run.getHlai(), previous_best_run.getUlbp2(), previous_best_run.getNkg2d(),
				previous_best_run.getMica(), previous_best_run.getIl15() };
		double previous_value = 0.5;
		if (batch > 1)
			previous_value = control_parameters_values[weight];
		for (double i = -0.0045; i < 0.005; i += 0.001) {
			control_parameters_values[weight] = previous_value + i * decimal_division;
			addRun(setUpRun(control_parameters_values[RESTING], control_parameters_values[HLAI], control_parameters_values[ULBP2],
					control_parameters_values[NKG2D], control_parameters_values[MICA], control_parameters_values[IL15]));
		}

		return runs;
	}
	
	private boolean canContinue() {
		return batch_controller.getParameterIndex() < 6;
	}
	
	private void runValuesForExperiment(int experiment, int ratio) {
		this.ratio = ratio;

		switch (experiment) {
		case 1: // resting
			setControlParameters(true, false, false, false, false, false);
			switch (ratio) {
			case 1:
				setExpectedCCellsByPercentage(14);
				break;
			case 2:
				setExpectedCCellsByPercentage(16);
				break;
			case 4:
				setExpectedCCellsByPercentage(19);
				break;
			case 8:
				setExpectedCCellsByPercentage(26);
				break;
			default:
				System.out.println("Non available ratio: " + String.valueOf(ratio));
				return;
			}
			break;
		case 2: // il15
			setControlParameters(false, false, false, false, false, true);
			switch (ratio) {
			case 1:
				setExpectedCCellsByPercentage(19);
				break;
			case 2:
				setExpectedCCellsByPercentage(27);
				break;
			case 4:
				setExpectedCCellsByPercentage(40);
				break;
			case 8:
				setExpectedCCellsByPercentage(45);
				break;
			default:
				System.out.println("Non available ratio: " + String.valueOf(ratio));
				return;
			}
			break;
		case 3: // ulbp2, nkg2d, mica
			setControlParameters(false, false, true, true, true, false);
			switch (ratio) {
			case 1:
				setExpectedCCellsByPercentage(10);
				break;
			case 2:
				setExpectedCCellsByPercentage(10);
				break;
			case 4:
				setExpectedCCellsByPercentage(11);
				break;
			case 8:
				setExpectedCCellsByPercentage(13);
				break;
			default:
				System.out.println("Non available ratio: " + String.valueOf(ratio));
				return;
			}
			break;
		case 4: // hlai
			setControlParameters(false, true, false, false, false, false);
			switch (ratio) {
			case 1:
				setExpectedCCellsByPercentage(42);
				break;
			case 2:
				setExpectedCCellsByPercentage(55);
				break;
			case 4:
				setExpectedCCellsByPercentage(66);
				break;
			case 8:
				setExpectedCCellsByPercentage(74);
				break;
			default:
				System.out.println("Non available ratio: " + String.valueOf(ratio));
				return;
			}
			break;
		case 5: // hlai, il15
			setControlParameters(false, true, false, false, false, true);
			switch (ratio) {
			case 1:
				setExpectedCCellsByPercentage(41);
				break;
			case 2:
				setExpectedCCellsByPercentage(57);
				break;
			case 4:
				setExpectedCCellsByPercentage(74);
				break;
			case 8:
				setExpectedCCellsByPercentage(87);
				break;
			default:
				System.out.println("Non available ratio: " + String.valueOf(ratio));
				return;
			}
			break;
		default:
			System.out.println("Unknown experiment " + String.valueOf(experiment));
			return;
		}
	}
		
	private void setExpectedCCellsByPercentage(Integer expected_ccells_percentage) {
		int volume = GlobalVariables.xDim * GlobalVariables.yDim * GlobalVariables.zDim;

		int ccellCount = (int) (volume / (this.ratio + 1));
		float expected_ccells_decimal = (float) expected_ccells_percentage / 100;
		this.expected_ccells = (int) ((int) ccellCount * (1 - expected_ccells_decimal));
	}
	
	private void setControlParameters(boolean resting, boolean hlai, boolean ulbp2, boolean nkg2d, boolean mica,
			boolean il15) {
		this.control_parameters_activation = new boolean[] { resting, hlai, ulbp2, nkg2d, mica, il15 };
	}

	private Run setUpRun(double resting, double hlai, double ulbp2, double nkg2d, double mica, double il15) {
		Run run = new Run();
		run.setRun_number(1);
		run.setStableTick(this.stable_tick);
		run.setCells_ratio(this.ratio);
		run.setResting_activation(this.control_parameters_activation[RESTING]);
		run.setHlai_activation(this.control_parameters_activation[HLAI]);
		run.setUlbp2_activation(this.control_parameters_activation[ULBP2]);
		run.setNkg2d_activation(this.control_parameters_activation[NKG2D]);
		run.setMica_activation(this.control_parameters_activation[MICA]);
		run.setIl15_activation(this.control_parameters_activation[IL15]);
		run.setResting(resting);
		run.setHlai(hlai);
		run.setUlbp2(ulbp2);
		run.setNkg2d(nkg2d);
		run.setMica(mica);
		run.setIl15(il15);

		return run;
	}

	private void addRun(Run run) {
		runs.add(run);
	}
}

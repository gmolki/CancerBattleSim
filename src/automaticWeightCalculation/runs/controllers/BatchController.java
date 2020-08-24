package automaticWeightCalculation.runs.controllers;

public class BatchController {
	private int batch;
	private int parameter_index;
	private boolean is_recalculating;
	private boolean[] control_parameters;
	private int num_control_parameters;

	public BatchController(boolean[] control_parameters) {
		this.batch = 1;
		this.is_recalculating = false;
		this.control_parameters = control_parameters;
		this.num_control_parameters = control_parameters.length;
		setFirstParameter();
	}

	public boolean nextBatch(boolean is_improving) {
		if (batch > 3 || !is_improving) {
			nextParameter();
			if (hasFirstCalculationEnded())
				startRecalculation();

			batch = 1;
		} else {
			batch++;
		}

		return is_recalculating;
	}

	public int getBatch() {
		return this.batch;
	}

	public int getParameterIndex() {
		return this.parameter_index;
	}
	
	public boolean isRecalculating() {
		return this.is_recalculating;
	}
	
	public int getDecimalDivision() {
		int decimal_division = 100;
		for (int i = 1; i < this.batch; i++) {
			decimal_division = decimal_division / 10;
		}
		return decimal_division;
	}

	private void setFirstParameter() {
		this.parameter_index = -1;
		nextParameter();
	}

	private void nextParameter() {
		do {
			this.parameter_index++;
		} while (!isParameterActivated(this.parameter_index));
	}

	private boolean hasFirstCalculationEnded() {
		return this.parameter_index > this.num_control_parameters - 1
				&& !this.is_recalculating
				&& numOfActivatedParameters() > 1;
	}

	private void startRecalculation() {
		setFirstParameter();
		this.is_recalculating = true;
	}

	private boolean isParameterActivated(int parameter_index) {
		try {
			return control_parameters[parameter_index];
		} catch (Exception e) {
			parameter_index++;
			return true;
		}

	}

	private int numOfActivatedParameters() {
		int count = 0;
		for (int i = 0; i < num_control_parameters; i++) {
			if (isParameterActivated(i))
				count++;
		}
		return count;
	}
}

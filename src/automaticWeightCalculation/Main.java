package automaticWeightCalculation;

import automaticWeightCalculation.calculator.OptimalWeightCalculator;
import utils.Archiver;
import utils.Timer;

public class Main {
	public static void main(String[] args) {
		int experiment = 2; // Coger desde los argumentos
		int ratio = 8; // Coger desde los argumentos

		Archiver.setUpWorkspace();

		Timer timer = new Timer();

		timer.start();

		OptimalWeightCalculator optimal_weight_calculator = new OptimalWeightCalculator(experiment, ratio);
		optimal_weight_calculator.calculateOptimalWeights();

		timer.stop();

		Archiver.writeResultsFile(experiment, ratio, optimal_weight_calculator.getResultsString(),
				timer.getElapsedTime());

		return;
	}

}

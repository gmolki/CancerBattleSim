package automaticWeightCalculation;

import automaticWeightCalculation.calculator.OptimalWeightCalculator;
import utils.Archiver;
import utils.Timer;

public class Main {
	public static void main(String[] args) {
		int experiment = 2; // Coger desde los argumentos
		int ratio = 4; // Coger desde los argumentos

		Archiver.setUpWorkspace();
		
//		runExperiment(experiment, ratio);
		runAllExperiments();
		return;
	}

	private static void runExperiment(int experiment, int ratio) {
		System.out.println("Running experiment: " + experiment + " ratio: " + ratio);
		Timer timer = new Timer();
		
		timer.start();
		
		OptimalWeightCalculator optimal_weight_calculator = new OptimalWeightCalculator(experiment, ratio);
		optimal_weight_calculator.calculateOptimalWeights();

		timer.stop();

		Archiver.writeResultsFile(experiment, ratio, optimal_weight_calculator.getResultsString(),
				timer.getElapsedTime());
	}

	private static void runAllExperiments() {
		Timer total_timer = new Timer();
		total_timer.start();
		for (int experiment = 1; experiment <= 5; experiment++) {
			for (int ratio = 1; ratio <= 8; ratio = ratio * 2) {
				System.out.println("Running experiment: " + experiment + " ratio: " + ratio);
				runExperiment(experiment, ratio);
				System.out
						.println("Elapsed time for experiment:" + String.valueOf(total_timer.getElapsedTime()) + " ms");
			}
		}
		total_timer.stop();
		System.out.println("Total elapsed time: " + String.valueOf(total_timer.getElapsedTime()) + " ms");
	}
}

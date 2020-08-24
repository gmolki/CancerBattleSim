package automaticWeightCalculation.calculator;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import automaticWeightCalculation.runs.Run;
import automaticWeightCalculation.runs.Runner;
import automaticWeightCalculation.runs.controllers.ExperimentRunsController;
import utils.Archiver;

public class OptimalWeightCalculator {
	private ExperimentRunsController experiment_runs_controller;
	private Runner runner;
	private Run parent_run;
	private Run best_run;
	private Integer best_approach;

	public OptimalWeightCalculator(int experiment, int ratio) {
		this.experiment_runs_controller = new ExperimentRunsController(experiment, ratio);
		this.parent_run = experiment_runs_controller.generateParentRun();
		this.setBestApproach(Integer.MAX_VALUE);
	}

	public Run calculateOptimalWeights() {
		Entry<Run, Integer> best_current_results = new SimpleEntry<Run, Integer>(this.parent_run,
				Integer.MAX_VALUE - 1);
		boolean is_improving = true;

		do {
			List<Run> runs = experiment_runs_controller.getRuns(best_current_results.getKey());
			executeRuns(runs);
			best_current_results = bestRunByApproach(runs);
			is_improving = setBetterResults(best_current_results);

		} while (experiment_runs_controller.nextSetOfRuns(is_improving) && !isPerfectApproach());

		return this.best_run;
	}

	public String getResultsString() {
		String results = best_run.toString();
		results += "expected_ccells:\t" + experiment_runs_controller.getExpectedCCells() + "\n";
		results += "remaining_ccells:\t" + best_approach + "\n";
		return results;
	}

	private void setUpRunner(Run run) {
		this.runner = new Runner(run.getParameters());
		try {
			runner.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void executeRun(Run run) {
		this.runner.runInitialize();

		for (int run_ticks = 0; run_ticks < (run.getStableTick()); run_ticks++) {
			this.runner.step();
			run_ticks += 1;
		}

		this.runner.stop();
		this.runner.cleanUpRun();
	}

	private void executeRuns(List<Run> runs) {
		for (Run run : runs) {
			this.setUpRunner(run);
			this.executeRun(run);
		}
		runner.cleanUpBatch();
	}

	private Entry<Run, Integer> bestRunByApproach(List<Run> runs) {
		Integer approach = Integer.MAX_VALUE;
		Map<String, String> best_current_run = null;
		System.out.println("Approach results: ");
		for (Entry<Map<String, String>, Map<String, String>> run_map : getRunsResults(Archiver.getOutputFiles(), runs)
				.entrySet()) {
			Map<String, String> run_results = run_map.getValue();

			Integer remaining_cccells = Integer.valueOf(run_results.get("remaining_cccells"));
			if (isBetterApproach(remaining_cccells, approach)) {
				approach = remaining_cccells;
				best_current_run = run_map.getKey();
			}
			System.out.println("\t" + String.valueOf(remaining_cccells));
		}

		return new SimpleEntry<Run, Integer>(Run.fromParametersMap(best_current_run), approach);
	}

	private boolean setBetterResults(Entry<Run, Integer> run_results) {
		int new_approach = run_results.getValue();
		if (isBetterApproach(new_approach, best_approach)) {
			best_run = run_results.getKey();
			setBestApproach(new_approach);
			return true;
		}
		return false;
	}

	private Map<Map<String, String>, Map<String, String>> getRunsResults(List<File> output_files, List<Run> runs) {
		Map<Map<String, String>, Map<String, String>> results = new HashMap<Map<String, String>, Map<String, String>>();
		try {
			for (File file : output_files) {
				Scanner scanner = new Scanner(file);
				String line = "";
				// Jump to the final results of a run
				while (scanner.hasNextLine()) {
					line = scanner.nextLine();
				}
				// Save parameters results in map
				String[] parameters = line.split(",");
				Map<String, String> run_results = new HashMap<String, String>();
				run_results.put("remaining_cccells", parameters[1]);
				run_results.put("remaining_nkcells", parameters[2]);
				results.put(getParamsMapOfRun(file), run_results);
				//
				scanner.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return results;
	}

	private void setBestApproach(Integer approach) {
		System.out.println("Expected CCells: " + String.valueOf(experiment_runs_controller.getExpectedCCells()));
		System.out.println("Best approach: " + String.valueOf(approach));
		best_approach = approach;
	}

	private boolean isBetterApproach(Integer new_approach, Integer best_approach) {
		int expected_ccells = experiment_runs_controller.getExpectedCCells();
		return Math.abs(new_approach - expected_ccells) < Math.abs(best_approach - expected_ccells);
	}

	private boolean isPerfectApproach() {
		return best_approach == experiment_runs_controller.getExpectedCCells();
	}

	private Map<String, String> getParamsMapOfRun(File file) {
		File params_file = Archiver.getParamsFileOf(file);
		Map<String, String> params = new HashMap<String, String>();
		try {
			Scanner scanner = new Scanner(params_file);
			String[] keys = scanner.nextLine().split(",");
			String[] values = scanner.nextLine().split(",");

			int i = 0;
			for (String key : keys) {
				params.put(key.substring(1, key.length() - 1), values[i].replaceAll("\"", ""));
				i++;
			}

			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return params;
	}
}

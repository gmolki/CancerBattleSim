package cancerBattleSim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import experiments.Run;

public class CancerBattleSimMain {
	static CancerBattleSimRunner runner;
	static List<Run> runs; // List containing all the runs that will be executed
	static Integer best_approach;
	static Run best_run;

	static File scenario_folder;
	static File output_folder;
	static Integer expected_ccells;
	static int cells_ratio;
	static int stable_tick;

	static boolean resting_activation;
	static boolean hlai_activation;
	static boolean ulbp2_activation;
	static boolean nkg2d_activation;
	static boolean mica_activation;
	static boolean il15_activation;

	/**
	 * 
	 * @return CancerBattleSimRunner object prepared to be initialized
	 */
	public static CancerBattleSimRunner setUpRunner(Run run) {
		CancerBattleSimRunner runner = new CancerBattleSimRunner(run.getParameters());
		try {
			runner.load(scenario_folder);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return runner;
	}

	/**
	 * 
	 * @return Run object with parameters loaded
	 */
	public static Run setUpRun(int run_number, int stable_tick, double resting, double hlai, double ulbp2, double nkg2d,
			double mica, double il15) {
		Run run = new Run();
		run.setRun_number(run_number);
		run.setStableTick(stable_tick);
		run.setCells_ratio(cells_ratio);
		run.setResting_activation(resting_activation);
		run.setHlai_activation(hlai_activation);
		run.setUlbp2_activation(ulbp2_activation);
		run.setNkg2d_activation(nkg2d_activation);
		run.setMica_activation(mica_activation);
		run.setIl15_activation(il15_activation);
		run.setResting(resting);
		run.setHlai(hlai);
		run.setUlbp2(ulbp2);
		run.setNkg2d(nkg2d);
		run.setMica(mica);
		run.setIl15(il15);

		return run;
	}

	public static void addRun(Run run) {
		runs.add(run);
	}

	public static void executeRun(Run run) {
		runner.runInitialize(run.getParameters());

		for (int run_ticks = 0; run_ticks < (run.getStableTick()); run_ticks++) {
			runner.step();
			run_ticks += 1;
		}

		runner.stop();
		runner.cleanUpRun();
	}

	private static void executeRuns(List<Run> runs) {
		for (Run run : runs) {
			runner = setUpRunner(run);

			executeRun(run);
		}
		runner.cleanUpBatch(); // Clean runner after all runs complete
	}

	public static Map<Map<String, String>, Map<String, String>> getRunsResults(List<File> output_files,
			List<Run> runs) {
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return results;
	}

	public static void calculateOptimalWeights(Run parent_run) {
		Entry<Run, Integer> best_current_results = new SimpleEntry<Run, Integer>(parent_run, Integer.MAX_VALUE - 1);

		int weight = nextWeight(0);
		int batch = 1;

		do {
			removePreviousOutputs();
			generateNewRuns(best_current_results.getKey(), weight, batch);
			executeRuns(runs);
			best_current_results = bestRunByApproach();
			if (!setBetterResults(best_current_results)) {
				weight = nextWeight(weight);
				batch = 1;
			} else {
				batch++;
			}

			if (batch > 3) {
				weight = nextWeight(weight);
				batch = 1;
			}
		} while (weight <= 6);

		printResults();

	}

	public static void main(String[] args) {
		// HACER QUE ESTOS PARAMETROS SE PASEN POR LOS ARGUMENTOS!!
		scenario_folder = new File("CancerBattleSim.rs");
		output_folder = new File("output/runs");
		expected_ccells = 1888;
		cells_ratio = 1;
		stable_tick = 1000;
		resting_activation = false; // Opcional argumento
		hlai_activation = true; // Opcional argumento
		ulbp2_activation = false; // Opcional argumento
		nkg2d_activation = false; // Opcional argumento
		mica_activation = true; // Opcional argumento
		il15_activation = false; // Opcional argumento
		//
		setBestApproach(Integer.MAX_VALUE);
		runner = new CancerBattleSimRunner();
		runs = new ArrayList<Run>();

		Run parent_run = setUpRun(1, stable_tick, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0); // First run with all values set at 0.0

		calculateOptimalWeights(parent_run);

		return;
	}

	private static void setBestApproach(Integer approach) {
		System.out.println("Best approach: " + String.valueOf(approach));
		best_approach = approach;
	}

	/**
	 * Removes all previous runs outputs files inside directory
	 * 'ProjectFolder/output/runs'
	 */
	private static void removePreviousOutputs() {
		try {
			FileUtils.cleanDirectory(output_folder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<File> getOutputFiles() {
		String[] files = output_folder.list();
		List<File> output_files = new ArrayList<File>();

		for (String fileName : files) {
			if (fileName.contains("output_data") && !fileName.contains("batch_param_map.txt"))
				output_files.add(new File("output/runs/" + fileName));
		}
		return output_files;
	}

	private static File getParamsFileOf(File file) {
		String file_path = file.toPath().toString();
		String params_file_path = file_path.substring(0, file_path.length() - 3) + "batch_param_map.txt";
		return new File(params_file_path);
	}

	private static Map<String, String> getParamsMapOfRun(File file) {
		File params_file = getParamsFileOf(file);
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return params;
	}

	private static Entry<Run, Integer> bestRunByApproach() {
		Integer approach = Integer.MAX_VALUE;
		Map<String, String> best_current_run = null;
		System.out.println("Approach results: ");
		for (Entry<Map<String, String>, Map<String, String>> run_map : getRunsResults(getOutputFiles(), runs)
				.entrySet()) {
			Map<String, String> run_results = run_map.getValue();

			Integer remaining_cccells = Integer.valueOf(run_results.get("remaining_cccells"));
			if (Math.abs(remaining_cccells - expected_ccells) < Math.abs(approach - expected_ccells)) {
				approach = remaining_cccells;
				best_current_run = run_map.getKey();
			}
			System.out.println("\t" + String.valueOf(remaining_cccells));
		}

		return new SimpleEntry<Run, Integer>(Run.fromParametersMap(best_current_run), approach);
	}

	private static boolean setBetterResults(Entry<Run, Integer> run_results) {
		if (isBetterThanPrevious(run_results.getValue())) {
			best_run = run_results.getKey();
			setBestApproach(run_results.getValue());
			return true;
		}
		return false;
	}

	private static boolean isBetterThanPrevious(Integer approach) {
		return Math.abs(approach - expected_ccells) < Math.abs(best_approach - expected_ccells);
	}

	private static boolean generateNewRuns(Run previous_best_run, int weight, int batch) {
		runs.clear();
		int decimal_division = getDecimalDivision(batch);
		double previous_value;
		switch (weight) {
		case 1: // resting
			previous_value = previous_best_run.getResting();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				Run run = setUpRun(1, stable_tick, new_value, previous_best_run.getHlai(), previous_best_run.getUlbp2(),
						previous_best_run.getNkg2d(), previous_best_run.getMica(), previous_best_run.getIl15());
				addRun(run);
			}
			return true;
		case 2: // hlai
			previous_value = previous_best_run.getHlai();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				Run run = setUpRun(1, stable_tick, previous_best_run.getResting(), new_value,
						previous_best_run.getUlbp2(), previous_best_run.getNkg2d(), previous_best_run.getMica(),
						previous_best_run.getIl15());
				addRun(run);
			}
			return true;
		case 3: // ulbp2
			previous_value = previous_best_run.getUlbp2();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				Run run = setUpRun(1, stable_tick, previous_best_run.getResting(), previous_best_run.getHlai(),
						new_value, previous_best_run.getNkg2d(), previous_best_run.getMica(),
						previous_best_run.getIl15());
				addRun(run);
			}
			return true;
		case 4: // nkg2d
			previous_value = previous_best_run.getNkg2d();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				Run run = setUpRun(1, stable_tick, previous_best_run.getResting(), previous_best_run.getHlai(),
						previous_best_run.getUlbp2(), new_value, previous_best_run.getMica(),
						previous_best_run.getIl15());
				addRun(run);
			}
			return true;
		case 5: // mica
			previous_value = previous_best_run.getMica();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				Run run = setUpRun(1, stable_tick, previous_best_run.getResting(), previous_best_run.getHlai(),
						previous_best_run.getUlbp2(), previous_best_run.getNkg2d(), new_value,
						previous_best_run.getIl15());
				addRun(run);
			}
			return true;
		case 6: // il15
			previous_value = previous_best_run.getIl15();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				Run run = setUpRun(1, stable_tick, previous_best_run.getResting(), previous_best_run.getHlai(),
						previous_best_run.getUlbp2(), previous_best_run.getNkg2d(), previous_best_run.getMica(),
						new_value);
				addRun(run);
			}
			return true;
		default:
			return false;
		}
	}

	private static int getDecimalDivision(int batch) {
		int decimal_division = 100;
		for (int i = 0; i < batch-1; i++) {
			decimal_division = decimal_division / 10;
		}
		return decimal_division;
	}

	private static int nextWeight(int weight) {
		do {
			weight++;
		} while (!isActivated(weight));

		return weight;
	}

	private static boolean isActivated(int weight) {
		switch (weight) {
		case 1: // resting
			return resting_activation;
		case 2: // hlai
			return hlai_activation;
		case 3: // ulbp2
			return ulbp2_activation;
		case 4: // nkg2d
			return nkg2d_activation;
		case 5: // mica
			return mica_activation;
		case 6: // il15
			return il15_activation;
		default:
			return true;
		}
	}

	private static void printResults() {
		System.out.println(best_run.getParametersString());
		System.out.println("Remaining CCells\t" + best_approach);
	}
}

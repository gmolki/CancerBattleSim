package automaticWeightCalculation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

import utils.Global;

public class Main {
	static File scenario_folder = new File("CancerBattleSim.rs");
	static File output_folder = new File("output/runs");
	static CancerBattleSimRunner runner;
	static BatchController batch_controller;

	static int stable_tick = 1000;
	static List<Run> runs; // List containing all the runs that will be executed

	// OPTIMAL PARAMETERS STORAGE
	static Integer best_approach;
	static Run best_run;
	//
	// CONTROL PARAMETERS
	static final int RESTING = 0;
	static final int HLAI = 1;
	static final int ULBP2 = 2;
	static final int NKG2D = 3;
	static final int MICA = 4;
	static final int IL15 = 5;
	static boolean[] control_parameters; // Array containing which control parameters are activated
	//
	static Integer expected_ccells;
	static int cells_ratio;

	public static void main(String[] args) {
		createOutputFolders();
		createBatchParametersFile();

		long startTime = System.currentTimeMillis();

		// Coger estos parámetros desde los argumentos
		int experiment = 1;
		int ratio = 1;

		runForExperiment(experiment, ratio);

		batch_controller = new BatchController(control_parameters);

		Run parent_run = createParentRun();
		best_run = parent_run;
		calculateOptimalWeights(parent_run);

		long stopTime = System.currentTimeMillis();

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String filename = "e" + experiment + "r" + ratio + "_" + timestamp.getTime() + ".txt";
		File result_file = new File("output/results/" + filename);
		FileWriter result_writer;
		try {
			result_file.createNewFile();
			result_writer = new FileWriter("output/results/" + filename);
			result_writer.write(resultsToString(startTime, stopTime));
			result_writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	static void calculateOptimalWeights(Run parent_run) {
		Entry<Run, Integer> best_current_results = new SimpleEntry<Run, Integer>(parent_run, Integer.MAX_VALUE - 1);

		do {
			clearPreviousRuns();
			generateNewRuns(best_current_results.getKey());
			executeRuns(runs);
			best_current_results = bestRunByApproach();
			boolean is_improving = setBetterResults(best_current_results);

			batch_controller.nextBatch(is_improving);
		} while (batch_controller.getParameterIndex() < 6 && !isPerfectApproach());
	}

	/**
	 * 
	 * @return CancerBattleSimRunner object ready to be initialized
	 */
	static CancerBattleSimRunner setUpRunner(Run run) {
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
	static Run setUpRun(double resting, double hlai, double ulbp2, double nkg2d,
			double mica, double il15) {
		Run run = new Run();
		run.setRun_number(1);
		run.setStableTick(stable_tick);
		run.setCells_ratio(cells_ratio);
		run.setResting_activation(control_parameters[RESTING]);
		run.setHlai_activation(control_parameters[HLAI]);
		run.setUlbp2_activation(control_parameters[ULBP2]);
		run.setNkg2d_activation(control_parameters[NKG2D]);
		run.setMica_activation(control_parameters[MICA]);
		run.setIl15_activation(control_parameters[IL15]);
		run.setResting(resting);
		run.setHlai(hlai);
		run.setUlbp2(ulbp2);
		run.setNkg2d(nkg2d);
		run.setMica(mica);
		run.setIl15(il15);

		return run;
	}

	static void addRun(Run run) {
		runs.add(run);
	}

	static void executeRun(Run run) {
		runner.runInitialize(run.getParameters());

		for (int run_ticks = 0; run_ticks < (run.getStableTick()); run_ticks++) {
			runner.step();
			run_ticks += 1;
		}

		runner.stop();
		runner.cleanUpRun();
	}

	static void executeRuns(List<Run> runs) {
		for (Run run : runs) {
			runner = setUpRunner(run);

			executeRun(run);
		}
		runner.cleanUpBatch(); // Clean runner after all runs complete
	}

	static Map<Map<String, String>, Map<String, String>> getRunsResults(List<File> output_files, List<Run> runs) {
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

	static void runForExperiment(int experiment, int ratio) {
		runner = new CancerBattleSimRunner();
		runs = new ArrayList<Run>();
		cells_ratio = ratio;
		
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
		setBestApproach(Integer.MAX_VALUE);
	}

	static Run createParentRun() {
		return setUpRun(0.0, 0.0, 0.0, 0.0, 0.0, 0.0); // First run with all values set at 0.0
	}
	
	static void setControlParameters(boolean resting, boolean hlai, boolean ulbp2, boolean nkg2d, boolean mica,
			boolean il15) {
		control_parameters = new boolean[] { resting, hlai, ulbp2, nkg2d, mica, il15 };
	}

	static void setExpectedCCellsByPercentage(Integer expected_ccells_percentage) {
		int volume = Global.xDim * Global.yDim * Global.zDim;

		int ccellCount = (int) (volume / (cells_ratio + 1));
		float foo = (float) expected_ccells_percentage / 100;
		expected_ccells = (int) ((int) ccellCount * (1 - foo));
	}

	static void setBestApproach(Integer approach) {
		System.out.println("Expected CCells: " + String.valueOf(expected_ccells));
		System.out.println("Best approach: " + String.valueOf(approach));
		best_approach = approach;
	}

	/**
	 * Removes all previous runs outputs files inside directory
	 * 'ProjectFolder/output/runs'
	 */
	static void clearPreviousRuns() {
		try {
			runs.clear();
			FileUtils.cleanDirectory(output_folder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static List<File> getOutputFiles() {
		String[] files = output_folder.list();
		List<File> output_files = new ArrayList<File>();

		for (String fileName : files) {
			if (fileName.contains("output_data") && !fileName.contains("batch_param_map.txt"))
				output_files.add(new File("output/runs/" + fileName));
		}
		return output_files;
	}

	static File getParamsFileOf(File file) {
		String file_path = file.toPath().toString();
		String params_file_path = file_path.substring(0, file_path.length() - 3) + "batch_param_map.txt";
		return new File(params_file_path);
	}

	static Map<String, String> getParamsMapOfRun(File file) {
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

	static Entry<Run, Integer> bestRunByApproach() {
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

	static boolean setBetterResults(Entry<Run, Integer> run_results) {
		if (isBetterThanPrevious(run_results.getValue())) {
			best_run = run_results.getKey();
			setBestApproach(run_results.getValue());
			return true;
		}
		return false;
	}

	static boolean isBetterThanPrevious(Integer approach) {
		return Math.abs(approach - expected_ccells) < Math.abs(best_approach - expected_ccells);
	}

	static void generateNewRuns(Run previous_best_run) {
		double previous_value = 0;
		int batch = batch_controller.getBatch();
		int decimal_division = getDecimalDivision(batch);

		switch (batch_controller.getParameterIndex()) {
		case RESTING:
			if (batch > 1 || !batch_controller.isRecalculating())
				previous_value = previous_best_run.getResting();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				addRun(setUpRun(new_value, previous_best_run.getHlai(), previous_best_run.getUlbp2(),
						previous_best_run.getNkg2d(), previous_best_run.getMica(), previous_best_run.getIl15()));
			}
			break;
		case HLAI:
			if (batch > 1 || !batch_controller.isRecalculating())
				previous_value = previous_best_run.getHlai();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				addRun(setUpRun(previous_best_run.getResting(), new_value, previous_best_run.getUlbp2(),
						previous_best_run.getNkg2d(), previous_best_run.getMica(), previous_best_run.getIl15()));
			}
			break;
		case ULBP2:
			if (batch > 1 || !batch_controller.isRecalculating())
				previous_value = previous_best_run.getUlbp2();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				addRun(setUpRun(previous_best_run.getResting(), previous_best_run.getHlai(), new_value,
						previous_best_run.getNkg2d(), previous_best_run.getMica(), previous_best_run.getIl15()));
			}
			break;
		case NKG2D:
			if (batch > 1 || !batch_controller.isRecalculating())
				previous_value = previous_best_run.getNkg2d();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				addRun(setUpRun(previous_best_run.getResting(), previous_best_run.getHlai(),
						previous_best_run.getUlbp2(), new_value, previous_best_run.getMica(),
						previous_best_run.getIl15()));
			}
			break;
		case MICA:
			if (batch > 1 || !batch_controller.isRecalculating())
				previous_value = previous_best_run.getMica();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				addRun(setUpRun(previous_best_run.getResting(), previous_best_run.getHlai(),
						previous_best_run.getUlbp2(), previous_best_run.getNkg2d(), new_value,
						previous_best_run.getIl15()));
			}
			break;
		case IL15:
			if (batch > 1 || !batch_controller.isRecalculating())
				previous_value = previous_best_run.getIl15();
			for (double i = 0.001; i < 0.01; i += 0.001) {
				double new_value = previous_value + i * decimal_division;
				addRun(setUpRun(previous_best_run.getResting(), previous_best_run.getHlai(),
						previous_best_run.getUlbp2(), previous_best_run.getNkg2d(), previous_best_run.getMica(),
						new_value));
			}
			break;
		default:
			System.out.println("Unknown parameter");
		}
	}

	static int getDecimalDivision(int batch) {
		int decimal_division = 100;
		for (int i = 1; i < batch; i++) {
			decimal_division = decimal_division / 10;
		}
		return decimal_division;
	}

	static boolean isPerfectApproach() {
		return best_approach == expected_ccells;
	}

	static String resultsToString(long start, long end) {
		String results = best_run.toString();
		results += "expected_ccells:  " + expected_ccells + "\n";
		results += "remaining_ccells: " + best_approach + "\n";
		results += "execution_time:   " + String.valueOf(end - start) + " ms\n";
		return results;
	}

	static void printResults(long start, long end) {
		System.out.println(resultsToString(start, end));
	}

	static void createOutputFolders() {
		File results = new File("output/results");
		if (!results.exists())
			results.mkdirs();
		File runs = new File("output/runs");
		if (!runs.exists())
			runs.mkdirs();
	}

	static void createBatchParametersFile() {
		File batch_folder = new File("batch");
		if (!batch_folder.exists())
			batch_folder.mkdirs();
		File batch_params = new File("batch/batch_params.xml");
		try {
			if (!batch_params.exists()) {
				batch_params.createNewFile();
				FileWriter writer = new FileWriter("batch/batch_params.xml");
				String params = "<?xml version=\"1.0\" ?><sweep runs=\"1\"><parameter name=\"mica\" type=\"constant\" constant_type=\"double\" value=\"0.0\"></parameter><parameter name=\"resting_activation\" type=\"constant\" constant_type=\"boolean\" value=\"true\"></parameter><parameter name=\"hlai_activation\" type=\"constant\" constant_type=\"boolean\" value=\"true\"></parameter><parameter name=\"il15\" type=\"constant\" constant_type=\"double\" value=\"0.0\"></parameter><parameter name=\"ulbp2_activation\" type=\"constant\" constant_type=\"boolean\" value=\"true\"></parameter><parameter name=\"cells_ratio\" type=\"constant\" constant_type=\"int\" value=\"8\"></parameter><parameter name=\"nkg2d_activation\" type=\"constant\" constant_type=\"boolean\" value=\"false\"></parameter><parameter name=\"mica_activation\" type=\"constant\" constant_type=\"boolean\" value=\"false\"></parameter><parameter name=\"hlai\" type=\"constant\" constant_type=\"double\" value=\"0.0\"></parameter><parameter name=\"nkg2d\" type=\"constant\" constant_type=\"double\" value=\"0.0\"></parameter><parameter name=\"il15_activation\" type=\"constant\" constant_type=\"boolean\" value=\"false\"></parameter><parameter name=\"weight_calculation\" type=\"constant\" constant_type=\"boolean\" value=\"true\"></parameter><parameter name=\"resting\" type=\"constant\" constant_type=\"double\" value=\"0.0\"></parameter><parameter name=\"ulbp2\" type=\"constant\" constant_type=\"double\" value=\"0.0\"></parameter><parameter name=\"randomSeed\" type=\"constant\" constant_type=\"int\" value=\"1\"></parameter></sweep>";
				writer.write(params);
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

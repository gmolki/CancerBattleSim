package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class Archiver {
	// Folders
	public static File scenario_folder = new File("CancerBattleSim.rs");
	private static File batch_folder = new File("batch");
	private static File output_folder = new File("output");
	public static File runs_folder = new File(output_folder.toPath() + "/runs");
	private static File results_folder = new File(output_folder.toPath() + "/results");

	// Files
	public static File batch_params = new File(batch_folder.toPath() + "/batch_params.xml");

	public static void setUpWorkspace() {
		createOutputFolders();
		createBatchParametersFile();
	}

	public static void writeResultsFile(int experiment, int ratio, String results, long elapsed_time) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String filename = "e" + experiment + "r" + ratio + "_" + timestamp.getTime() + ".txt";
		File result_file = new File(results_folder.toPath() + "/" + filename);
		FileWriter result_writer;
		try {
			result_file.createNewFile();
			result_writer = new FileWriter(results_folder.toPath() + "/" + filename);
			result_writer.write(results);
			result_writer.write("Elapsed time:\t " + elapsed_time);
			result_writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File getParamsFileOf(File file) {
		String file_path = file.toPath().toString();
		String params_file_path = file_path.substring(0, file_path.length() - 3) + "batch_param_map.txt";
		return new File(params_file_path);
	}

	public static List<File> getOutputFiles() {
		String[] files = runs_folder.list();
		List<File> output_files = new ArrayList<File>();

		for (String fileName : files) {
			if (fileName.contains("output_data") && !fileName.contains("batch_param_map.txt"))
				output_files.add(new File(runs_folder.toPath() + "/" + fileName));
		}
		return output_files;
	}

	private static void createOutputFolders() {
		if (!results_folder.exists())
			results_folder.mkdirs();
		if (!runs_folder.exists())
			runs_folder.mkdirs();
	}

	private static void createBatchParametersFile() {
		if (!batch_folder.exists())
			batch_folder.mkdirs();
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

	
	public static void cleanRunsFolder() {
		try {
			FileUtils.cleanDirectory(runs_folder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

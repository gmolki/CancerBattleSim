package cancerBattleSim;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import experiments.Run;

public class CancerBattleSimMain {
	static File scenario_folder;
	static File output_folder;
	static int expected_nkcells;
	static int cells_ratio;
	
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
	 * @param run_number
	 * @param cells_ratio
	 * @param resting_activation
	 * @param hlai_activation
	 * @param ulbp2_activation
	 * @param nkg2d_activation
	 * @param mica_activation
	 * @param il15_activation
	 * @param resting
	 * @param hlai
	 * @param ulbp2
	 * @param nkg2d
	 * @param mica
	 * @param il15
	 * @return Run object with parameters loaded
	 */
	public static Run setUpRun(int run_number, double resting, double hlai, double ulbp2, double nkg2d, double mica, double il15) {
		Run run = new Run();
		run.setRun_number(run_number);
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
	
	public static void executeRun(CancerBattleSimRunner runner, Run run) {
		runner.runInitialize(run.getParameters());
		
		for (int run_ticks = 0; run_ticks < (run.getStableTick()); run_ticks++) {
			runner.step();
			run_ticks += 1;
		}

		runner.stop();
		runner.cleanUpRun();
	}

	/**
	 * TODO:
	 * 1 - Terminar clase Run.java #DONE# 
	 * 2 - Aplicar algoritmo 
	 * 2.0 - Revisar resultado de una run (output file)
	 * 2.1 - Asignar valores a las variables de cada run #DONE#
	 * 2.2 - Ejecutar 1 run que aumente un parametero de control diferente
	 * 2.3 - Mirar que resultado ha sido el óptimo y seguir por ese camino
	 * 2.4 - Volver a 2.2 con los parametros igual a los resultados óptimos
	 */
	public static void main(String[] args) {
		// HACER QUE ESTOS PARAMETROS SE PASEN POR LOS ARGUMENTOS!!
		scenario_folder = new File("CancerBattleSim.rs");
		output_folder = new File("output/runs");
		expected_nkcells = 50;
		cells_ratio = 8;
		resting_activation = false; // Opcional argumento
		hlai_activation = false; // Opcional argumento
		ulbp2_activation = false; // Opcional argumento
		nkg2d_activation = false; // Opcional argumento
		mica_activation = false; // Opcional argumento
		il15_activation = false; // Opcional argumento
		// 
		
		removePreviousOutputs(); // Elimina los resultados que se hayan obtenido en otras ejecuciones
		
		// First run with all values at 0.0
		Run parent_run = setUpRun(1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		CancerBattleSimRunner parent_runner = setUpRunner(parent_run);
		executeRun(parent_runner, parent_run);
		
		File output = getOutputFile(); // VOY POR AQUI!!
		
		// verificar que estoy cogiendo el archivo correcto
		// leer el resultado de la run con x run_number
		
		
		
//		// START LOOP FOR ALL RUNS
//		Run run = parent_run;
//		CancerBattleSimRunner runner = setUpRunner(parent_run);
//		// START Run
//		for(int i = 0; i < 2; i++) {
//			runner.runInitialize(run.getParameters());
//	
//			for (int run_ticks = 0; run_ticks < (run.getStableTick()); run_ticks++) {
//				runner.step();
//				run_ticks += 1;
//			}
//	
//			runner.stop();
//			runner.cleanUpRun();
//		}
//		// END Run
//		// END LOOP FOR ALL RUNS
//
//		runner.cleanUpBatch(); // Clean runner after all runs complete

		return;
	}
	
	private static void removePreviousOutputs() {
		try {
			FileUtils.cleanDirectory(output_folder);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	private static File getOutputFile() {
		String[] files = output_folder.list();
		
		for( String fileName : files )
	       {
	           if( fileName.contains("output_data") && !fileName.contains("batch_param_map.txt") ) 
	                return new File(output_folder.toURI() + fileName);
	       }
		return null;
	}
}

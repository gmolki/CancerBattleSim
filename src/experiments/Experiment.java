package experiments;

public class Experiment {
	/****** CONTROL PARAMETERS ***/
	private final int RESTING = 0;
	private final int IL15 = 1;
	private final int NKG2D = 2;
	private final int MICA = 3;
	private final int ULBP2 = 4;
	private final int HLAI = 5;
	/*****************************/
	private int num_control_parameters = 6; // RESTING, IL15, ULBP2, MICA, NKG2D, HLAI
	private int num_cell_features = 5; // kill_chance, kill_distance, lose_distance, speed, multiply_chance

	private int control_parameters[] = new int[num_control_parameters];
	private double cell_fetures[] = new double[num_cell_features];
	private double weights[] = new double[num_cell_features];

	public Experiment(double kill_chance, double kill_distance, double lose_distance, double speed,
			double multiply_chance, int RESTING, int IL15, int ULBP2, int MICA, int NKG2D, int HLAI) {

	}
}
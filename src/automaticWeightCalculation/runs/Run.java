package automaticWeightCalculation.runs;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import repast.simphony.batch.parameter.ParameterLineParser;
import repast.simphony.parameter.Parameters;
import utils.Archiver;

public class Run {
	int run_number;
	boolean resting_activation, hlai_activation, ulbp2_activation, nkg2d_activation, mica_activation, il15_activation;
	double resting, hlai, ulbp2, nkg2d, mica, il15;
	int cells_ratio;
	boolean weight_calculation = true;

	int stable_tick = 300; // Tick when runs are used to be stable

	/**
	 * Set default run parameters. By default all control parameters are set to 0 an
	 * deactivate.
	 */
	public Run() {
		this.run_number = 1;
		this.resting_activation = false;
		this.hlai_activation = false;
		this.ulbp2_activation = false;
		this.nkg2d_activation = false;
		this.mica_activation = false;
		this.il15_activation = false;
		this.resting = 0.0;
		this.hlai = 0.0;
		this.ulbp2 = 0.0;
		this.nkg2d = 0.0;
		this.mica = 0.0;
		this.il15 = 0.0;
		this.cells_ratio = 1;
	}

	public int getRepetitions() {
		return stable_tick;
	}

	public void setStableTick(int stable_tick) {
		this.stable_tick = stable_tick;
	}

	public int getStableTick() {
		return stable_tick * 2;
	}

	public int getRun_number() {
		return run_number;
	}

	public void setRun_number(int run_number) {
		this.run_number = run_number;
	}

	public boolean isResting_activation() {
		return resting_activation;
	}

	public void setResting_activation(boolean resting_activation) {
		this.resting_activation = resting_activation;
	}

	public boolean isHlai_activation() {
		return hlai_activation;
	}

	public void setHlai_activation(boolean hlai_activation) {
		this.hlai_activation = hlai_activation;
	}

	public boolean isUlbp2_activation() {
		return ulbp2_activation;
	}

	public void setUlbp2_activation(boolean ulbp2_activation) {
		this.ulbp2_activation = ulbp2_activation;
	}

	public boolean isNkg2d_activation() {
		return nkg2d_activation;
	}

	public void setNkg2d_activation(boolean nkg2d_activation) {
		this.nkg2d_activation = nkg2d_activation;
	}

	public boolean isMica_activation() {
		return mica_activation;
	}

	public void setMica_activation(boolean mica_activation) {
		this.mica_activation = mica_activation;
	}

	public boolean isIl15_activation() {
		return il15_activation;
	}

	public void setIl15_activation(boolean il15_activation) {
		this.il15_activation = il15_activation;
	}

	public double getResting() {
		return resting;
	}

	public void setResting(double resting) {
		this.resting = resting;
	}

	public double getHlai() {
		return hlai;
	}

	public void setHlai(double hlai) {
		this.hlai = hlai;
	}

	public double getUlbp2() {
		return ulbp2;
	}

	public void setUlbp2(double ulbp2) {
		this.ulbp2 = ulbp2;
	}

	public double getNkg2d() {
		return nkg2d;
	}

	public void setNkg2d(double nkg2d) {
		this.nkg2d = nkg2d;
	}

	public double getMica() {
		return mica;
	}

	public void setMica(double mica) {
		this.mica = mica;
	}

	public double getIl15() {
		return il15;
	}

	public void setIl15(double il15) {
		this.il15 = il15;
	}

	public int getCells_ratio() {
		return cells_ratio;
	}

	public void setCells_ratio(int cells_ratio) {
		this.cells_ratio = cells_ratio;
	}

	public boolean isWeight_calculation() {
		return weight_calculation;
	}

	public void setWeight_calculation(boolean weight_calculation) {
		this.weight_calculation = weight_calculation;
	}

	public Parameters getParameters() {
		String params = this.getParametersString();
		ParameterLineParser parser;
		try {
			parser = new ParameterLineParser(Archiver.batch_params.toURI());
			return parser.parse(params);
		} catch (Exception e) {
			System.out.println("Error while parsing parameters");
			e.printStackTrace();
			return null;
		}
	}

	public String getParametersString() {
		String parameters = String.valueOf(getRun_number()) + "\t";
		parameters += "resting_activation\t" + String.valueOf(isResting_activation()) + ",";
		parameters += "hlai_activation\t" + String.valueOf(isHlai_activation()) + ",";
		parameters += "ulbp2_activation\t" + String.valueOf(isUlbp2_activation()) + ",";
		parameters += "nkg2d_activation\t" + String.valueOf(isNkg2d_activation()) + ",";
		parameters += "mica_activation\t" + String.valueOf(isMica_activation()) + ",";
		parameters += "il15_activation\t" + String.valueOf(isIl15_activation()) + ",";
		parameters += "resting\t" + String.valueOf(getResting()) + ",";
		parameters += "hlai\t" + String.valueOf(getHlai()) + ",";
		parameters += "ulbp2\t" + String.valueOf(getUlbp2()) + ",";
		parameters += "nkg2d\t" + String.valueOf(getNkg2d()) + ",";
		parameters += "mica\t" + String.valueOf(getMica()) + ",";
		parameters += "il15\t" + String.valueOf(getIl15()) + ",";
		parameters += "cells_ratio\t" + String.valueOf(getCells_ratio()) + ",";
		parameters += "weight_calculation\t" + String.valueOf(isWeight_calculation()) + ",";
		parameters += "randomSeed\t" + String.valueOf(1);

		return parameters;
	}

	public String toString() {
		String parameters = "resting_activation:\t" + String.valueOf(isResting_activation()) + "\n";
		parameters += "hlai_activation:\t" + String.valueOf(isHlai_activation()) + "\n";
		parameters += "ulbp2_activation:\t" + String.valueOf(isUlbp2_activation()) + "\n";
		parameters += "nkg2d_activation:\t" + String.valueOf(isNkg2d_activation()) + "\n";
		parameters += "mica_activation:\t" + String.valueOf(isMica_activation()) + "\n";
		parameters += "il15_activation:\t" + String.valueOf(isIl15_activation()) + "\n";
		parameters += "resting:\t" + String.valueOf(getResting()) + "\n";
		parameters += "hlai:\t" + String.valueOf(getHlai()) + "\n";
		parameters += "ulbp2:\t" + String.valueOf(getUlbp2()) + "\n";
		parameters += "nkg2d:\t" + String.valueOf(getNkg2d()) + "\n";
		parameters += "mica:\t" + String.valueOf(getMica()) + "\n";
		parameters += "il15:\t" + String.valueOf(getIl15()) + "\n";
		parameters += "cells_ratio:\t" + String.valueOf(getCells_ratio()) + "\n";

		return parameters;
	}

	public static Run fromParametersMap(Map<String, String> run_parameters) {
		Run run = new Run();
		for (Map.Entry<String, String> parameter : run_parameters.entrySet()) {
			try {
				BeanUtils.setProperty(run, parameter.getKey(), parameter.getValue());
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return run;
	}
}

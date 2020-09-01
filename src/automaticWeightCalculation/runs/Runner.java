package automaticWeightCalculation.runs;

import repast.simphony.batch.BatchScenarioLoader;
import repast.simphony.engine.controller.Controller;
import repast.simphony.engine.controller.DefaultController;
import repast.simphony.engine.environment.AbstractRunner;
import repast.simphony.engine.environment.ControllerRegistry;
import repast.simphony.engine.environment.DefaultRunEnvironmentBuilder;
import repast.simphony.engine.environment.RunEnvironmentBuilder;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.parameter.Parameters;
import repast.simphony.parameter.SweeperProducer;
import simphony.util.messages.MessageCenter;
import utils.Archiver;

public class Runner extends AbstractRunner {
	private static MessageCenter msgCenter = MessageCenter.getMessageCenter(Runner.class);

	private RunEnvironmentBuilder runEnvironmentBuilder;
	protected Controller controller;
	protected boolean pause = false;
	protected Object monitor = new Object();
	protected SweeperProducer producer;
	private ISchedule schedule;
	private Parameters parameters;

	public Runner(Parameters parameters) {
		runEnvironmentBuilder = new DefaultRunEnvironmentBuilder(this, true);
		controller = new DefaultController(runEnvironmentBuilder);
		controller.setScheduleRunner(this);
		this.parameters = parameters;
	}

	public void load() throws Exception {
		if (Archiver.scenario_folder.exists()) {
			BatchScenarioLoader loader = new BatchScenarioLoader(Archiver.scenario_folder);
			ControllerRegistry registry = loader.load(runEnvironmentBuilder);
			controller.setControllerRegistry(registry);
		} else {
			msgCenter.error("Scenario not found",
					new IllegalArgumentException("Invalid scenario " + Archiver.scenario_folder.getAbsolutePath()));
			return;
		}
		controller.batchInitialize();
		controller.runParameterSetters(this.parameters);
	}

	public void runInitialize() {
		controller.runInitialize(this.parameters);
		schedule = RunState.getInstance().getScheduleRegistry().getModelSchedule();
	}

	public void cleanUpRun() {
		controller.runCleanup();
	}

	public void cleanUpBatch() {
		controller.batchCleanup();
	}

	// Step the schedule
	public void step() {
		schedule.execute();
	}

	// stop the schedule
	public void stop() {
		if (schedule != null)
			schedule.executeEndActions();
	}
	
	@Override
	public void execute(RunState toExecuteOn) {
		// TODO Auto-generated method stub
	}
}

package cancerBattleSim;

import java.io.File;

import repast.simphony.batch.BatchScenarioLoader;
import repast.simphony.engine.controller.Controller;
import repast.simphony.engine.controller.DefaultController;
import repast.simphony.engine.environment.AbstractRunner;
import repast.simphony.engine.environment.ControllerRegistry;
import repast.simphony.engine.environment.DefaultRunEnvironmentBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunEnvironmentBuilder;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.parameter.Parameters;
import repast.simphony.parameter.SweeperProducer;
import simphony.util.messages.MessageCenter;

public class CancerBattleSimRunner extends AbstractRunner {
	private static MessageCenter msgCenter = MessageCenter.getMessageCenter(CancerBattleSimRunner.class);

	private RunEnvironmentBuilder runEnvironmentBuilder;
	protected Controller controller;
	protected boolean pause = false;
	protected Object monitor = new Object();
	protected SweeperProducer producer;
	private ISchedule schedule;
	private Parameters parameters;

	public CancerBattleSimRunner(Parameters parameters) {
		runEnvironmentBuilder = new DefaultRunEnvironmentBuilder(this, true);
		controller = new DefaultController(runEnvironmentBuilder);
		controller.setScheduleRunner(this);
		this.parameters = parameters;
	}

	public CancerBattleSimRunner() {
	}

	public void load(File scenarioDir) throws Exception {
		if (scenarioDir.exists()) {
			BatchScenarioLoader loader = new BatchScenarioLoader(scenarioDir);
			ControllerRegistry registry = loader.load(runEnvironmentBuilder);
			controller.setControllerRegistry(registry);
		} else {
			msgCenter.error("Scenario not found",
					new IllegalArgumentException("Invalid scenario " + scenarioDir.getAbsolutePath()));
			return;
		}
		controller.batchInitialize();
		controller.runParameterSetters(parameters);
	}

	public void runInitialize(Parameters parameters) {

			controller.runInitialize(parameters);
			schedule = RunState.getInstance().getScheduleRegistry().getModelSchedule();
	}

	public void cleanUpRun() {
		controller.runCleanup();
	}

	public void cleanUpBatch() {
		controller.batchCleanup();
	}

	// returns the tick count of the next scheduled item
	public double getNextScheduledTime() {
		return ((Schedule) RunEnvironment.getInstance().getCurrentSchedule()).peekNextAction().getNextTime();
	}

	// returns the number of model actions on the schedule
	public int getModelActionCount() {
		return schedule.getModelActionCount();
	}

	// returns the number of non-model actions on the schedule
	public int getActionCount() {
		return schedule.getActionCount();
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

	public void setFinishing(boolean fin) {
		schedule.setFinishing(fin);
	}

	@Override
	public void execute(RunState toExecuteOn) {
		// TODO Auto-generated method stub
	}
}

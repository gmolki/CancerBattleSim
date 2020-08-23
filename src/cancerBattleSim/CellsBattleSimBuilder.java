package cancerBattleSim;

import cancerBattleSim.experiment.Experiment;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import utils.Global;

public class CellsBattleSimBuilder implements ContextBuilder<Object> {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("CancerBattleSim");
		Parameters params = RunEnvironment.getInstance().getParameters();
		Experiment experiment = new Experiment(context, params.getBoolean("resting_activation"),
				params.getBoolean("il15_activation"), params.getBoolean("ulbp2_activation"),
				params.getBoolean("mica_activation"), params.getBoolean("nkg2d_activation"),
				params.getBoolean("hlai_activation"));

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), Global.xDim, Global.yDim, Global.zDim);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(new WrapAroundBorders(),
				new SimpleGridAdder<Object>(), true, Global.xDim, Global.yDim, Global.zDim));

		context = experiment.setExperiment(space, grid);

		return context;
	}
}

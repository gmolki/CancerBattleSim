package cancerBattleSim;

import cells.CCell;
import cells.NCell;
import cells.NKCell;
import experiments.Experiment;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
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
		Experiment experiment = new Experiment(context, 0, 1, 0, 0, 0, 0);

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), experiment.getxDim(), experiment.getyDim(),
				experiment.getzDim());

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(new WrapAroundBorders(),
				new SimpleGridAdder<Object>(), true, experiment.getxDim(), experiment.getyDim(), experiment.getzDim()));

		context = experiment.setExperiment(space, grid);

		return context;
	}
}

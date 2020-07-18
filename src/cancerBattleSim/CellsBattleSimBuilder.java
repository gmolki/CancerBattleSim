package cancerBattleSim;

import cells.CCell;
import cells.NCell;
import cells.NKCell;
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

	private int xDim = 40, yDim = 40, zDim = 4, ncellCount = 0, nkcellCount = 0, ccellCount = 0;

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	@Override
	public Context build(Context<Object> context) {
		context.setId("CancerBattleSim");

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), xDim, yDim, zDim);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(new WrapAroundBorders(),
				new SimpleGridAdder<Object>(), true, xDim, yDim, zDim));

		// Creation of new Cells and adding it to the simulation space
		context = createCellsForRatio(context);

		return context;
	}

	private Context<Object> createCellsForRatio(Context<Object> context) {
		Parameters params = RunEnvironment.getInstance().getParameters();
		Global.RATIO = params.getFloat("cells_ratio");

		calculateCellsForRatio();

		params.setValue("ccell_count", ccellCount);
		for (int i = 0; i < ccellCount; i++) {
			context.add(new CCell(space, grid));
		}

		params.setValue("ncell_count", ncellCount);
		for (int i = 0; i < ncellCount; i++) {
			context.add(new NCell(space, grid));
		}

		params.setValue("nkcell_count", nkcellCount);
		for (int i = 0; i < nkcellCount; i++) {
			context.add(new NKCell(space, grid));
		}

		for (Object cell : context) {
			NdPoint pt = space.getLocation(cell);
			grid.moveTo(cell, (int) pt.getX(), (int) pt.getY(), (int) pt.getZ());
		}

		return context;
	}

	private void calculateCellsForRatio() {
		int volume = xDim * yDim * zDim;

		ccellCount = (int) (volume / (Global.RATIO + 1));
		nkcellCount = volume - ccellCount;
	}
}

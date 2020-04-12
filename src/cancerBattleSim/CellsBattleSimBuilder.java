package cancerBattleSim;

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

public class CellsBattleSimBuilder implements ContextBuilder<Object> {

	private int xDim = 15;
	private int yDim = 15;
	private int zDim = 15;
	
	@Override
	public Context build(Context<Object> context) {
		context.setId("CancerBattleSim");
		
		ContinuousSpaceFactory spaceFactory =
				ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = 
				spaceFactory.createContinuousSpace("space", context,
						new RandomCartesianAdder<Object>(),
						new repast.simphony.space.continuous.WrapAroundBorders(),
						xDim, yDim, zDim);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(),
						true, xDim, yDim, zDim));
		
		// Creation of new Cells and adding it to the simulation space
		Parameters params = RunEnvironment.getInstance().getParameters();
		int ccellCount = params.getInteger("ccell_count");
		for (int i = 0; i < ccellCount; i++) {
			context.add(new CCell(space, grid));
		}
		
		int ncellCount = params.getInteger("ncell_count");
		for (int i = 0; i < ncellCount; i++) {
			context.add(new NCell(space, grid));
		}
		
		int nkellCount = params.getInteger("nkcell_count");
		for (int i = 0; i < nkellCount; i++) {
			context.add(new NKCell(space, grid));
		}
		
		for (Object cell : context) {
			NdPoint pt = space.getLocation(cell);
			grid.moveTo(cell, (int)pt.getX(), (int)pt.getY(), (int)pt.getZ());
		}
		
		return context;
	}

}

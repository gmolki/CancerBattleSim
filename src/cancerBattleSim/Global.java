package cancerBattleSim;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;

public class Global {

	public static float RATIO = 8;
	public static double KILL_CHANCE = 0.6;

	public static int dormants = 0;

	public static void moveTowards (Object agent, Object target, double distance,
			ContinuousSpace<Object> space, Grid<Object> grid) {
		if (target == null) {
			double x_distance = RandomHelper.nextIntFromTo(-1, 1) * distance,
					y_distance = RandomHelper.nextIntFromTo(-1, 1) * distance,
					z_distance = RandomHelper.nextIntFromTo(-1, 1) * distance;

			NdPoint newLocation = 
					space.moveByDisplacement(agent, x_distance, y_distance, z_distance);

			grid.moveTo(agent, 
					(int)newLocation.getX(),
					(int)newLocation.getY(),
					(int)newLocation.getZ());
		} else {
			NdPoint agent_position = space.getLocation(agent);
			NdPoint target_position = space.getLocation(target);
			double[] angle = angleFor3DMovement(agent_position, target_position);
			agent_position = space.moveByVector(agent, distance, angle);
			grid.moveTo(agent,
					(int)agent_position.getX(),
					(int)agent_position.getY(),
					(int)agent_position.getZ());
		}
	}

	public static double[] angleFor3DMovement(NdPoint pt1, NdPoint pt2) {
		double x = pt1.getX() - pt2.getX();
		double y = pt1.getY() - pt2.getY();
		double z = pt1.getZ() - pt2.getZ();
		
		return new double[] {
				Math.atan2(Math.sqrt(Math.pow(y,2)+Math.pow(z, 2)), x),
				Math.atan2(Math.sqrt(Math.pow(z,2)+Math.pow(x, 2)), y),
				Math.atan2(Math.sqrt(Math.pow(x,2)+Math.pow(y, 2)), z)};		
	}
}

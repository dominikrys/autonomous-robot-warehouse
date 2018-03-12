package main;

import java.io.IOException;

import actions.Movement;
import communication.RobotNetworkHandler;
import interfaces.Action;
import lejos.nxt.LightSensor;
import lejos.util.Delay;
import rp.systems.StoppableRunnable;

public class RobotController implements StoppableRunnable {
	private Movement move;
	private final LightSensor LEFT_SENSOR;
	private final LightSensor RIGHT_SENSOR;
	private boolean running;
	private RobotNetworkHandler networkHandler;

	public RobotController() {
		LEFT_SENSOR = new LightSensor(Configuration.LEFT_LIGHT_SENSOR);
		RIGHT_SENSOR = new LightSensor(Configuration.RIGHT_LIGHT_SENSOR);
		move = new Movement(calibrate());
		running = true;
		networkHandler = new RobotNetworkHandler();
	}

	@Override
	public void run() {
		networkHandler.run();
		Action currentCommand = null;
		int pickAmount = 0;
		while (running) {
			try {
				// Print message
				currentCommand = (Action) networkHandler.receiveAction();

				if (currentCommand != null) {
					// Print Message
					if (currentCommand.equals(Action.PICKUP) || currentCommand.equals(Action.DROPOFF)) {
						pickAmount = (int) networkHandler.receiveInt();
					}

					move.nextAction(currentCommand, pickAmount);

				} else {
					System.out.println("Error: No command received");
					break;
				}
			} catch (IOException e) {
				System.out.println("Couldn't receive object in RobotController" + e.getMessage());
			}

		}
		try {
			networkHandler.sendObject(Action.ACTION_COMPLETE);
		} catch (IOException e) {
			System.out.println("Couldn't send object in RobotController" + e.getMessage());
		}
	}

	@Override
	public void stop() {
		running = false;

	}

	public int calibrate() {
		Delay.msDelay(500);
		return (LEFT_SENSOR.readValue() + RIGHT_SENSOR.readValue()) / 2;
	}

	public static void main(String[] args) {
		RobotController rc = new RobotController();
		rc.run();
	}

}
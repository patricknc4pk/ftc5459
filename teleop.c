#pragma config(Hubs,  S1, HTMotor,  HTMotor,  HTMotor,  HTServo)
#pragma config(Sensor, S1,     ,               sensorI2CMuxController)
#pragma config(Motor,  mtr_S1_C1_1,     driveR,        tmotorTetrix, openLoop, encoder)
#pragma config(Motor,  mtr_S1_C1_2,     driveL,        tmotorTetrix, openLoop, reversed, encoder)
#pragma config(Motor,  mtr_S1_C2_1,     conveyor,      tmotorTetrix, openLoop)
#pragma config(Motor,  mtr_S1_C2_2,     lift,          tmotorTetrix, openLoop, reversed) // up is +, down is -
#pragma config(Motor,  mtr_S1_C3_1,     powerLifterR,  tmotorTetrix, openLoop)
#pragma config(Motor,  mtr_S1_C3_2,     powerLifterL,  tmotorTetrix, openLoop, reversed)
#pragma config(Servo,  srvo_S1_C4_1,    door,                 tServoStandard)

#include "JoystickDriver.c"  // handles Bluetooth messages

// discreteButtons() - handles all the buttons that manipulate discrete variables.
// We need this to reduce jitter with these buttons while not lagging the main task.
task discreteButtons() {
	// initialization: set the servos/variables
	servo[door] = 0;

	while(true) {
		// door control: if the button is pressed, toggle the door
		if (joy1Btn(6) || joy2Btn(6)) {
			servo[door] = servo[door] == 0 ? 255 : 0; // if less than halfway open, open all the way; else close
			wait1Msec(1000); // reduces jitter
		}
	}
}

// main() - the main task for the robot, essentially an infinite loop.
// This must be the last routine in the file.
// Game controller information is sent every ~50 milliseconds from the Field Management System (FMS).
// At the end of the tele-op period, the FMS will automatically halt execution.
task main() {
	waitForStart(); // wait for start of tele-op phase

	// initialization: set all motors to zero power (do we really need this?) No you don't
	motor[driveL] = 0;
	motor[driveR] = 0;
	motor[lift] = 0;
	motor[conveyor] = 0;

	StartTask(discreteButtons); // start the discrete button handler

	// the main loop - handles all the continuous controls on the joystick
	while(true) {
		getJoystickSettings(joystick); // updates joystick info every 50-100ms

		// drivetrain: if the joystick is outside the dead zone, set the corresponding motor to the value; else 0
		// TODO: get a more sophisticated model
		motor[driveL] = abs(joystick.joy1_y1) > 5 ? joystick.joy1_y1 : 0;
		motor[driveR] = abs(joystick.joy1_y2) > 5 ? joystick.joy1_y2 : 0;

		// power lifters: same as above
		motor[powerLifterL] = abs(joystick.joy2_y1) > 5 ? joystick.joy2_y1 : 0;
		motor[powerLifterR] = abs(joystick.joy2_y2) > 5 ? joystick.joy2_y2 : 0;

		// lift control: run the lift in forward/reverse if the suitable buttons are pressed
		// this is a continuous control, thus it doesn't belong in discreteButtons()
		/* the nested ternary statement is the equivalent of saying:
			if the up button (5) is pressed, raise the lift. else...
				if the down button (7) is pressed, lower the lift. else...
					set the motor to zero.
		*/
		motor[lift] = joy1Btn(5) || joy2Btn(5) ? 75 : joy1Btn(7) || joy2Btn(7) ? -30 : 0;

		// conveyor control: if the button is pressed, run the conveyor
		// since this is a continuous button, it doesn't belong in discreteButtons()
		motor[conveyor] = joy1Btn(8) ? 75 : joy1Btn(3) ? -75 : 0;
	}
}
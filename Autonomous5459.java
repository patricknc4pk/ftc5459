package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsAnalogOpticalDistanceSensor;

public class Autonomous5459 extends LinearOpMode {
    public Autonomous5459() { }

    // DC motors
    DcMotor drive_left_front;
    DcMotor drive_left_back;
    DcMotor drive_right_front;
    DcMotor drive_right_back;

    /* SERVOS */
    Servo ziplineLeft;
    Servo ziplineRight;
    Servo rodLeft;
    Servo rodRight;
    Servo rodCenter;
    Servo wire;
    Servo push;

    /* TITLES */
    public static final String DLF = "Drive_Front_Left";
    public static final String DLB = "Drive_Back_Left";
    public static final String DRF = "Drive_Front_Right";
    public static final String DRB = "Drive_Back_Right";
    public static final String ZL = "ZiplineLeft";
    public static final String ZR = "ZiplineRight";
    public static final String RL = "RodLeft";
    public static final String RC = "RodCenter";
    public static final String RR = "RodRight";
    public static final String WS = "Wire";
    public static final String PS = "Push";

    /* ZIPLINE VALUES */
    public static final double SPALeft = 0.0;
    public static final double SPARight = 1.0;
    public static final double SPCLeft = 0.5;
    public static final double SPCRight = 0.5;
    public static final double SPBLeft = 0.9;
    public static final double SPBRight = 0.1;
    public static final double RCi = 0.0;
    public static final double RLi = 0.0;
    public static final double RRi = 1.0;
    public static final double PSi = 0.0;

    public ModernRoboticsI2cGyro gyro;
    public ModernRoboticsI2cColorSensor color;
    public ModernRoboticsAnalogOpticalDistanceSensor opticalLeft;
    public ModernRoboticsAnalogOpticalDistanceSensor opticalRight;

    //double gyro_rotations;

    //PID
    long lastTime;
    double Input, Output, Setpoint;
    double errSum, lastInput;
    double kp, ki, kd;
    int SampleTime = 1000; //**

    void PID() {
        long now = System.currentTimeMillis();
        int timeChange = ((int) now - (int) lastTime);
        if (timeChange >= SampleTime) {
            double error = Setpoint - Input; //error
            errSum += error;
            double dInput = (Input - lastInput);

            Output = kp * error + ki * errSum - kd * dInput;

            lastInput = Input;
            lastTime = now;
        }
    }

    void Tune(double Kp, double Ki, double Kd) {
        double SampleTimeInSec = ((double) SampleTime / 1000);
        kp = Kp;
        ki = Ki * SampleTimeInSec;
        kd = Kd / SampleTimeInSec;
    }

    void SetSampleTime(int NewSampleTime) {
        if (NewSampleTime > 0) {
            double ratio = (double) NewSampleTime / (double) SampleTime;
            ki *= ratio;
            kd /= ratio;
            SampleTime = (int) NewSampleTime;
        }
    }

    public void turn_gyro_nPID(double angle, double midPower) {

        double currentHeading = gyro.getHeading();
        double driveGain = 3;

        while (currentHeading < angle) {

            double headingError = angle - currentHeading;
            double driveSteering = headingError * driveGain;
            double leftPower = midPower + driveSteering;
            double rightPower = midPower - driveSteering;

            drive_left_front.setPower(Math.signum(angle) * leftPower);
            drive_left_back.setPower(Math.signum(angle) * leftPower);
            drive_right_front.setPower(Math.signum(angle) * rightPower);
            drive_right_back.setPower(Math.signum(angle) * rightPower);

            currentHeading = gyro.getHeading();
        }
        drive_left_front.setPower(0);
        drive_left_back.setPower(0);
        drive_right_front.setPower(0);
        drive_right_back.setPower(0);
    } // this might not work at all.

    public void turn_gyro_PID(double angle, double power) {
        // double gyro_prior = gyro.getHeading();
        double angle_rad = (angle * 2 * Math.PI) / 360;
        while (Output < Math.abs(angle_rad)) {
            drive_left_front.setPower(Math.signum(angle) * power);
            drive_left_back.setPower(Math.signum(angle) * power);
            drive_right_front.setPower(-1 * Math.signum(angle) * power);
            drive_right_back.setPower(-1 * Math.signum(angle) * power);
            // getHeading();
        }
        drive_left_front.setPower(0);
        drive_left_back.setPower(0);
        drive_right_front.setPower(0);
        drive_right_back.setPower(0);
    }

    public void release_lift() {

    }


    @Override
    public void runOpMode() throws InterruptedException {
        drive_left_front = hardwareMap.dcMotor.get(DLF);
        drive_left_front.setDirection(DcMotor.Direction.REVERSE);

        drive_left_back = hardwareMap.dcMotor.get(DLB);
        //MotorBackLeft.setDirection(DcMotor.Direction.REVERSE);

        //MotorLift = hardwareMap.dcMotor.get("LiftAngle");

        drive_right_front = hardwareMap.dcMotor.get(DRF);
        drive_right_back = hardwareMap.dcMotor.get(DRB);
        drive_right_back.setDirection(DcMotor.Direction.REVERSE);

        wire = hardwareMap.servo.get(WS);
        rodCenter = hardwareMap.servo.get(RC);
        rodLeft = hardwareMap.servo.get(RL);
        rodRight = hardwareMap.servo.get(RR);

        push = hardwareMap.servo.get(PS);

        gyro = (ModernRoboticsI2cGyro) hardwareMap.gyroSensor.get("Gyro");
        color = (ModernRoboticsI2cColorSensor) hardwareMap.colorSensor.get("Color");
        opticalLeft = (ModernRoboticsAnalogOpticalDistanceSensor) hardwareMap.opticalDistanceSensor.get("OpticalLeft");
        opticalRight = (ModernRoboticsAnalogOpticalDistanceSensor) hardwareMap.opticalDistanceSensor.get("OpticalRight");

        double gyro_rotations = 0;
        int v_state = 0;
        int counter = 0;
        int threshold = 200;

        // SERVO INITIALIZATIONS: CRITICAL

        ziplineLeft.setPosition(0.5);
        ziplineRight.setPosition(0.5);
        rodLeft.setPosition(0.5);
        rodRight.setPosition(0.5);
        rodCenter.setPosition(0.5);
        wire.setPosition(1.0);
        push.setPosition(1.0);


        waitOneFullHardwareCycle();

        gyro.calibrate();

        while (gyro.isCalibrating()) {
            Thread.sleep(50);
        }

        waitForStart();


        while (opModeIsActive()) {
            switch (v_state) {

                case 0:
                    wire.setPosition(0.5);
                    v_state++;
                    break;
                
                case 1:


            }
        }
    }
}
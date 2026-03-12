from gpiozero import LED, RotaryEncoder, Button, DistanceSensor, MotionSensor
from signal import pause # Para mantener el programa vivo sin usar un while True pesado
import board
import adafruit_dht
import json
import time

class SensorManager:
    def __init__(self):
        # LEDs / actuators-
        self.brake_led = LED(5)
        self.slow_led = LED(6)       # Green
        self.normal_led = LED(13)    # Yellow
        self.fast_led = LED(26)      # Red
        self.person_led = LED(18)    # Blue
        self.object_led = LED(16)    # White

        # --- Inputs (sensors) ---
        self.distance_sensor = DistanceSensor(
            echo=24,
            trigger=23,
            threshold_distance=0.15
        )
        self.motion_sensor = MotionSensor(25)
        self.encoder = RotaryEncoder(17, 27)
        self.brake_button = Button(22, bounce_time=0.1)
        self.climate_sensor = adafruit_dht.DHT11(board.D4)

    def read_climate(self):
        while True:
            try:
                return self.climate_sensor.temperature, self.climate_sensor.humidity
            except RuntimeError:
                # DHT11 readings often fail sometimes
                return None, None

    def connect_car(self, car):
        """Connect physical inputs to car actions."""
        self.encoder.when_rotated_clockwise = car.increase_speed
        self.encoder.when_rotated_counter_clockwise = car.decrease_speed

        self.brake_button.when_pressed = car.start_braking
        self.brake_button.when_released = car.stop_braking

        # Safety sensors
        self.distance_sensor.when_in_range = car.object_emergency
        self.distance_sensor.when_out_of_range = car.resume_drive

        self.motion_sensor.when_motion = car.person_emergency
        self.motion_sensor.when_no_motion = car.resume_drive
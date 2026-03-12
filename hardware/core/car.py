from gpiozero import LED, RotaryEncoder, Button, DistanceSensor, MotionSensor
from signal import pause
import json
import time
class Car:
    def __init__(self, id_coche, person_led, object_led, stop_led, slow_led, normal_led, fast_led, brake_button, mensajero, sensor_manager):
        self.id_coche = id_coche
        self.speed = 0
        self.humidity = 0.0
        self.temperature = 0.0
        self.is_brake_on = False
        self.stop_temperature = False
        self.alerta_velocidad_enviada = False

        self.person_led = person_led
        self.object_led = object_led
        self.stop_led = stop_led
        self.slow_led = slow_led
        self.normal_led = normal_led
        self.fast_led = fast_led
        self.brake_button = brake_button
        self.mensajero = mensajero
        self.sensor_manager = sensor_manager
        
        


    def readData(self):
        contador = 0
        while True: 
            # TIEMPO REAL (Cada 0.1s) 
            self.verificar_objetos() 
            self.verificar_personas()

            # LÓGICA DE ENVÍO 
            enviar_ahora = False

            # Caso A: Han pasado 10 segundos (Reporte normal)
            if contador >= 100:
                enviar_ahora = True
                contador = 0
            
            # Caso B: Emergencia por velocidad (Solo si no hemos avisado ya)
            if self.speed > 130:
                if not self.alerta_enviada:
                    enviar_ahora = True
                    self.alerta_enviada = True  
            else:
                
                self.alerta_enviada = False

            # --- SECCIÓN 3: EL ENVÍO REAL ---
            if enviar_ahora:
                t, h = self.sensor_manager.read_climate()
                if t is not None:
                    self.temperature = t
                    self.humidity = h
                    self.temperature_danger()
                    print(f"Update data: {t}º {h}%")
                
                if self.mensajero:
                    self.mensajero.enviar_telematria(self)
                else:
                    print("Modo local: Datos leídos pero no enviados. 🛰️")

            # Pausa obligatoria de 0.1s para que la CPU pare
            time.sleep(0.1)
            contador += 1

    def start_braking(self):
        self.decrease_speed()
        self.is_brake_on = True
        self.stop_led.on()
    
    def stop_braking(self):
        self.is_brake_on = False
        self.stop_led.off()


    def update_speed_leds(self):
        self.slow_led.off()
        self.normal_led.off()
        self.fast_led.off()

        if self.speed > 0: 
            if self.speed <= 40:
                self.slow_led.on()
            elif 40 < self.speed <= 70:
                self.normal_led.on()
            else:
                self.fast_led.on()

    def increase_speed(self):
        if not self.is_brake_on:
            self.speed = min(170, self.speed + 10)
            self.update_speed_leds()

            if self.speed == 170:
                print("Maximum speed: 170 km/h")
            else:
                print(f"Speed: {self.speed} km/h 🏎️")
        else:
            print("Danger detected: cannot move")

    def decrease_speed(self):
        self.speed = max(0, self.speed - 10)
        self.update_speed_leds()
        print(f"Braking... Speed: {self.speed} km/h 📉")

    def object_emergency(self):
        self.speed = 0
        self.is_brake_on = True

        self.person_led.off()
        self.object_led.on()
        self.stop_led.on()
        self.update_speed_leds()

        print("EMERGENCY! Object detected at less than 20 cm 🚗🚨")

    def person_emergency(self):
        self.speed = 0
        self.is_brake_on = True

        self.object_led.off()
        self.person_led.on()
        self.stop_led.on()
        self.update_speed_leds()

        print("EMERGENCY! Pedestrian detected 🚶‍♂️🚨")
    
    def temperature_danger(self):
        if self.temperature >= 130:
            self.speed = 0
            self.stop_led.on()
            self.is_brake_on = True
            self.stop_temperature = True
            print("¡EMERGENCIA! Motor sobrecalentado (>130°C). Deteniendo... 🔥")
        elif self.temperature <130 and self.stop_temperature == True:
            print("Motor enfriado. El sistema de seguridad permite reanudar... ✅")
            self.stop_temperature = False  # 'Limpiamos' la memoria 
            self.resume_drive()
    
    def verificar_objetos(self):
        distancia = self.sensor_manager.distance_sensor.distance

        if distancia < 0.10:
            self.object_emergency()
        else:
            self.object_led.off()
            self.resume_drive()
 
    def verificar_personas(self):
        hay_persona = self.sensor_manager.motion_sensor.motion_detected 

        if hay_persona:
            self.person_emergency()
        else:
            self.person_led.off()
        self.resume_drive()


    def resume_drive(self):
        if self.brake_button.is_pressed:
            self.stop_led.on()
            self.is_brake_on = True
        elif not self.object_led.is_lit and not self.person_led.is_lit and not self.stop_temperature:
            self.stop_led.off()
            self.is_brake_on = False
            self.person_led.off()
            


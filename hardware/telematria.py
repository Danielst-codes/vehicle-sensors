from gpiozero import LED, RotaryEncoder, Button, DistanceSensor, MotionSensor
from signal import pause # Para mantener el programa vivo sin usar un while True pesado
import board
import adafruit_dht
import json
import time

# --- CONFIGURACIÓN DE HARDWARE ---
freno = LED(5)          
led_lento = LED(6)      # Verde 🟢
led_normal = LED(13)    # Amarillo 🟡
led_rapido = LED(26)    # Rojo 🔴
led_persona = LED(18)   # Azul 🔵
led_objeto = LED(16)    # Blanco ⚪

# Trigger=23, Echo=24. Ponemos el límite (threshold) a 20cm (0.2m)
# 'queue_len' es cuántas lecturas promedia. Si lo bajamos, es más rápido 
sensor_distancia = DistanceSensor(echo=24, trigger=23, threshold_distance=0.15)
sensor_pir = MotionSensor(25)
encoder = RotaryEncoder(17, 27) # Quitamos el max_steps para manejarlo manual
boton_freno = Button(22, bounce_time=0.1)
sensor_clima = adafruit_dht.DHT11(board.D4)
# --- VARIABLES DE ESTADO ---
velocidad = 0

# --- FUNCIONES DE LÓGICA ---

def actualizar_leds():
    """Controla qué LED de velocidad se enciende según la variable global."""
    # Primero apagamos todos los de velocidad
    led_lento.off()
    led_normal.off()
    led_rapido.off()
    
    if 0 < velocidad <= 40:
        led_lento.on()
    elif 40 < velocidad <= 70:
        led_normal.on()
    elif velocidad > 70:
        led_rapido.on()

def aumentar_velocidad():
    global velocidad
    velocidad = min(100, velocidad + 10)

    if velocidad == 100:
        print("Velocidad maxima 100km/h")
    else:
        print(f"Velocidad: {velocidad} km/h 🏎️")
    actualizar_leds()

def bajar_velocidad():
    global velocidad
    velocidad = max(0, velocidad - 10)

    print(f"Frenando... Velocidad: {velocidad} km/h 📉")
    actualizar_leds()

def emergencia_objeto():
    global velocidad
    velocidad = 0
    freno.on()
    led_objeto.on()
    actualizar_leds() # Esto apagará los LEDs verde/amarillo/rojo
    print("¡EMERGENCIA! Objeto detectado a < 20cm 🚗🚨")

def emergencia_peaton():
    global velocidad
    velocidad = 0
    freno.on()
    led_persona.on()
    actualizar_leds()
    print("¡EMERGENCIA! Peatón detectado 🚶‍♂️🚨")

def reanudar_marcha():
    freno.off()
    led_objeto.off()
    led_persona.off()
    print("Camino despejado. El coche está parado, gire el encoder. ✅")

# ---Envio de datos
def obtener_telemetria():
    try:
        temp = sensor_clima.temperature
        hum = sensor_clima.humidity
        
        # Si la lectura es correcta, creamos el "paquete"
        if temp is not None and hum is not None:
            datos = {
                "velocidad": velocidad,
                "temperatura": temp,
                "humedad": hum,
                "timestamp": time.time()
            }
            return json.dumps(datos).encode('utf-8')
        
    except RuntimeError as error:
        # El DHT11 a veces falla, simplemente lo intentamos de nuevo después
        print(f"Error de lectura: {error.args[0]}")
    
    return None

# --- ASIGNACIÓN DE EVENTOS (Fuera del bucle) ---

# Encoder y  Boton
encoder.when_rotated_clockwise = aumentar_velocidad
boton_freno.when_pressed = lambda: (bajar_velocidad(), freno.on())
boton_freno.when_released = freno.off

# Sensores (Seguridad Automática)
sensor_distancia.when_in_range = emergencia_objeto
sensor_distancia.when_out_of_range = reanudar_marcha

sensor_pir.when_motion = emergencia_peaton
sensor_pir.when_no_motion = reanudar_marcha

#Tiempo de envio los datos
ultimo_envio = time.time()
print("🏎️ Sistema de Telemetría Online. ¡Usa el encoder!")

while True:
    tiempo_actual = time.time()

    if(tiempo_actual - ultimo_envio >= 2):
        obtener_telemetria()
        print("Enviando datos")

        #Actualiamos el cronometro para que vuelva a contar
        ultimo_envio = tiempo_actual

        #PAra no estresar al procesador
        time.sleep(0.1)


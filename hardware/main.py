from core.car import Car
from messaging.messenger import MessagesSender
from sensors.hardwareManager import SensorManager
from signal import pause
import threading

IP_ORDENADOR = '192.168.1.38'
mensajero_datos = MessagesSender(IP_ORDENADOR, queue_name='telemetria_coche')
#mensajero_datos.conectar()


sensor_manager = SensorManager()
coche = Car(
    id_coche = "1234FHS",
    person_led = sensor_manager.person_led,
    object_led = sensor_manager.object_led,
    stop_led   = sensor_manager.brake_led,
    slow_led   = sensor_manager.slow_led,
    normal_led = sensor_manager.normal_led,
    fast_led   = sensor_manager.fast_led,
    brake_button = sensor_manager.brake_button,
    mensajero = mensajero_datos,
    sensor_manager = sensor_manager
)

sensor_manager.connect_car(coche)

#Creamos un hilo para el sensor de temp y hum 
hilo_clima = threading.Thread(target=coche.readData)

# Lo marcamos como "daemon" para que se cierre automáticamente si el programa principal se apaga
hilo_clima.daemon = True
hilo_clima.start()
pause()
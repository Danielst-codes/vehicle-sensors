import pika
from datetime import datetime
import json
import time

class MessagesSender:
    def __init__(self, host, queue_name = 'telemetria_coche'):
        self.host = host
        self.queue_name = queue_name
        self.connection = None
        self.channel = None
   
    def conectar(self):
        #Establecemos conexion y con el canal
        parametros = pika.ConnectionParameters(host=self.host)
        self.connection = pika.BlockingConnection(parametros)
       
        #Creamos el canal
        self.channel = self.connection.channel()

        #Declaramos la cola
        self.channel.queue_declare(queue=self.queue_name)
        print(f"Conexion establecida {self.host}")

    #Funcion para recibir los datos y enviar
    def enviar_telematria(self, car_objeto):
        self.asegurar_conexion()

        if self.connection is None or not self.channel.is_open:
            print("Imposible enviar datos: No hay conexión con el servidor 📡❌")
            return
        try:
            message = {
                "id_coche" : car_objeto.id_coche,
                "velocidad": car_objeto.speed,
                "temperatura": car_objeto.temperature,
                "humedad": car_objeto.humidity,
                "parado": car_objeto.is_brake_on,
                "fecha": datetime.now().strftime("%H:%M:%S")
            }
            print("enviando datos")
            #Lo convertimos en un json 
            message_final = json.dumps(message)

            self.channel.basic_publish(
                    exchange='',
                    routing_key=self.queue_name,
                    body=message_final
            )
            print(f"Mensaje enviado : {message_final}")
        except Exception as e:
                self.connection = None
                self.channel = None
                print(f"La conexión se perdió durante el envío: {e} 🌪️")
        
    
    def asegurar_conexion(self):
        if self.connection is None or self.connection.is_closed:
            try:
                print("Intentando conectar con RabbitMQ... 🔌")
                self.conectar()
            except Exception as e:
                print(f"Error de conexión: {e}. Reintentando en 5s... ⏳")
                self.connection = None
                self.channel = None
                time.sleep(5)   
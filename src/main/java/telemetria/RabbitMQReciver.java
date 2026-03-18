package telemetria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import respuestas.RabbitMQOrdenesSender;
import sensor.SensorData;
import static net.logstash.logback.argument.StructuredArguments.fields;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.DomainCombiner;


    public class RabbitMQReciver {
        private final String QUEUE_NAME = "telemetria_coche";
        private RabbitMQOrdenesSender emisorOrdenes;
        private Channel channel;
        private Connection connection;
        private static final Logger logger = LoggerFactory.getLogger(RabbitMQReciver.class);
        private int puerto; 

        public RabbitMQReciver() {

        try (BufferedReader br = new BufferedReader(new FileReader("conexion_config.txt"))) {
            String linea;
            linea = br.readLine();
            if (linea != null){
                 puerto = Integer.parseInt(linea);
            }
               
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(puerto);

            while (this.channel == null) { // Se queda aquí hasta que conecte
                try {
                    this.connection = factory.newConnection();
                    this.channel = connection.createChannel();
                    this.channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                    this.emisorOrdenes = new RabbitMQOrdenesSender(); 
                    logger.info("🎧 Conectado a RabbitMQ. Escuchando red...");
                } catch (Exception e) {
                    logger.warn("⚠️ RabbitMQ no responde todavía... reintentando en 5 segundos");
                    try { Thread.sleep(5000); } catch (InterruptedException ie) {}
                }
            }
        }
    public void consumirMensaje(){
        try{
            ObjectMapper mapper = new ObjectMapper();
           

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                byte[] paquetBytes = delivery.getBody();

                try {
                    SensorData datosObjeto = mapper.readValue(paquetBytes, SensorData.class);
                    
                    if (datosObjeto.temperatura > 120 ){
                        logger.error("¡Peligro! Temperatura crítica: {}°C", datosObjeto.temperatura, fields(datosObjeto));
                        this.emisorOrdenes.enviarOrden(datosObjeto.getVelocidad());
                    }else if (datosObjeto.velocidad > 120) {
                        logger.warn("⚠️ ALERTA DE SEGURIDAD: Velocidad muy alta: {} Km/H",datosObjeto.velocidad, fields(datosObjeto));
                        this.emisorOrdenes.enviarOrden(datosObjeto.getVelocidad() - 120);
                    }else if (datosObjeto.humedad > 80) {
                        logger.error("PEligro de humedad en el motor: {} %", datosObjeto.humedad, fields(datosObjeto));
                    } 
                    else {
                        logger.info("Lectura de telemetría recibida", fields(datosObjeto));
                    }

                } catch(Exception e) {
                    logger.error("Error al traducir el paquete: " + e.getMessage());
                }
            };
            channel.basicConsume(QUEUE_NAME,true, deliverCallback, consumerTag -> { });
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
}

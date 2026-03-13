package respuestas;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import sensor.SensorData;

import java.sql.Timestamp;
import java.util.Queue;

public class RabbitMQOrdenesSender {
    private static final String  QUEUE_NAME = "ordenes_coche";
    private Connection connection;
    private Channel channel;
    ObjectMapper traductor = new ObjectMapper();

    public RabbitMQOrdenesSender() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq");
        factory.setPort(5672);

            while (this.channel == null) {
                try {
                    this.connection = factory.newConnection();
                    this.channel = connection.createChannel();
                    this.channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                } catch (Exception e) {
                    try { Thread.sleep(2000); } catch (InterruptedException ie) {}
                }
            }   
    }
    public void enviarOrden(Double frenar){
        try{
            byte[] mensaje = traductor.writeValueAsBytes(frenar);
            channel.basicPublish("",QUEUE_NAME, null, mensaje);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
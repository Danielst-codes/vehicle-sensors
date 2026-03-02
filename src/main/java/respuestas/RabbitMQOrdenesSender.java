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

    public RabbitMQOrdenesSender(){
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(5672);

            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            this.channel.queueDeclare(QUEUE_NAME, false, false,false, null);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void enviarOrden(String orden){
        try{
            byte[] mensaje = traductor.writeValueAsBytes(orden);
            channel.basicPublish("",QUEUE_NAME, null, mensaje);
            System.out.println("⚠️ mensaje enviado");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
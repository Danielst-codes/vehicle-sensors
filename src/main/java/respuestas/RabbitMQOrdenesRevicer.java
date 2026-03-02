package respuestas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class RabbitMQOrdenesRevicer {
    private static final String QUEUE_NAME = "ordenes_coche";
    private Connection connection;
    private Channel channel;

    public RabbitMQOrdenesRevicer(){
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setPort(5672);
            factory.setHost("localhost");

            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            this.channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            System.out.println("Recibiendo mensajes");


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public  void recibirOrden(){
        try{
            ObjectMapper mapper = new ObjectMapper();
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                byte[] mensajeBytes = delivery.getBody();

                try{
                    String mensaje = mapper.readValue(mensajeBytes, String.class);
                }catch (Exception e ){
                    e.printStackTrace();
                }
            };
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

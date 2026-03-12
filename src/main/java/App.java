
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telemetria.RabbitMQReciver;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("🚀 Iniciando Sistema de Monitorización IoT...");

        try {
            // 1. Creamos el receptor (que ahora también gestiona el envío de órdenes)
            RabbitMQReciver receptor = new RabbitMQReciver();

            // 2. Iniciamos la escucha de mensajes de telemetría
            receptor.consumirMensaje();

            logger.info("✅ Sistema listo y esperando datos del coche...");

        } catch (Exception e) {
            logger.error("❌ Error fatal al iniciar el monitor: " + e.getMessage());
        }
    }
}
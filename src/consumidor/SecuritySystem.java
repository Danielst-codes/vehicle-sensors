package consumidor;

import memoria.BufferFIFO;
import sensor.SensorData;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SecuritySystem { 
    private BufferFIFO buffer;
    private SensorData lastData;
    private List<SensorData> pastData;
    private static final double UMBRAL_AMBERTENCIA = 20.0;
    private static final double  UMBRAL_CRITICO = 50.0;

    public SecuritySystem(BufferFIFO buffer){
        this.buffer = buffer;
        this.lastData = null; 
        this.pastData = new ArrayList<>();
    }

    public void monitorizar(){
        SensorData currentData = buffer.obtenerDatos();
        
        if (currentData == null){
            return; 
        }

        try {
            if (this.lastData != null) {
                double velocidadDelta = currentData.getVelocidad() - this.lastData.getVelocidad();
                if (velocidadDelta > UMBRAL_CRITICO){
                    gestionarAtaqueCritico(velocidadDelta);
                }else if (velocidadDelta >= UMBRAL_AMBERTENCIA) {
                    gestionarAdvertencia(velocidadDelta);
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void gestionarAdvertencia(double velocidadDelta) {
        System.out.println("[WARNING] inyeccion delta: " + velocidadDelta);
    }

    private void gestionarAtaqueCritico(double velocidadDelta) {
        System.out.println("[CRITICAL] inyeccion delta: " + velocidadDelta);
    }
}

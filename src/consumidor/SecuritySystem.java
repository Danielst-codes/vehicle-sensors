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
                double diferencia = currentData.getVelocidad() - this.lastData.getVelocidad();
            }
        }
    }
}

package memoria;

import java.util.LinkedList;
import java.util.Queue;

import sensor.SensorData;

public class BufferFIFO {
    Queue<SensorData> buffer = new LinkedList<>();

    public void agregarDatos(SensorData data){
        buffer.add(data);
    }

    public SensorData obtenerDatos(){
        return buffer.poll();
    }
}

package sensor;
import java.sql.Timestamp;

public class SensorData{
    private Double velocidad;
    private  Double temperatura;
    private Timestamp timestamp;

    public SensorData(Double velocidad, Double temperatura, Timestamp timestamp){
        this.velocidad = velocidad;
        this.temperatura = temperatura;
        this.timestamp = timestamp;
    }

    public Double  getVelocidad(){
        return this.velocidad;
    }

    public Double  getTemperatura(){
        return this.temperatura;
    }   
    
    public Timestamp  getTimestamp() {
        return this.timestamp;
    }

    public void setVelocidad(Double velocidad){
        this.velocidad = velocidad;
    }

    public void setTemperatura(Double temperatura){
        this.temperatura = temperatura;
    }

    public void setTimestamp(Timestamp timestamp){
        this.timestamp = timestamp;
    }

    public String toString(){
        return "velocidad: " + this.velocidad + ", temperatura: " + this.temperatura + ", timestamp: " + this.timestamp.toString();
    }

}

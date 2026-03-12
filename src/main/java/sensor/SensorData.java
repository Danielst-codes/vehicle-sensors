package sensor;
import java.sql.Date;
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorData {
    public String id_coche;
    public Double velocidad;
    public Double temperatura;
    public Double humedad;
    public Boolean parado;
    public String fecha;

    public SensorData(){}

    public SensorData(String id, Double vel, Double temp, Double hum, Boolean stop, String date) {
        this.id_coche = id;
        this.velocidad = vel;
        this.temperatura = temp;
        this.humedad = hum;
        this.parado = stop;
        this.fecha = date;
    }
    public String getId_coche() {
        return this.id_coche;
    }

    public void setId_coche(String id_coche) {
        this.id_coche = id_coche;
    }

    public Double getVelocidad() {
        return this.velocidad;
    }

    public void setVelocidad(Double velocidad) {
        this.velocidad = velocidad;
    }

    public Double getTemperatura() {
        return this.temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getHumedad() {
        return this.humedad;
    }

    public void setHumedad(Double humedad) {
        this.humedad = humedad;
    }

    public Boolean getParado() {
        return this.parado;
    }

    public void setParado(Boolean parado) {
        this.parado = parado;
    }

    public String getFecha() {
        return this.fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "velocidad: " + this.velocidad + ", temperatura: " + this.temperatura + ", fecha: " + this.fecha;
    }

}

package car;
import java.sql.Timestamp;

import memoria.BufferFIFO;
import sensor.SensorData;

public class CarSimulator {
    private Double velocidadActual;
    private Double temperaturaActual;
    private BufferFIFO buffer;

    public CarSimulator(Double velocidadActual, Double temperaturaActual, BufferFIFO buffer){
        this.velocidadActual = velocidadActual;
        this.temperaturaActual = temperaturaActual;
        this.buffer = buffer;
    }

    public void acelerar(Double incremetno){
        this.velocidadActual += incremetno;
    }

    public void frenar(Double decremento){
        this.velocidadActual -= decremento;
    }

    public void calentar(Double incremento){
        this.temperaturaActual += incremento;
    }

    public void enfriar(Double decremento){
        this.temperaturaActual -= decremento;
    }

    public Double getVelocidadActual(){
        return this.velocidadActual;
    }
    public Double getTemperaturaActual(){
        return this.temperaturaActual;
    }
    public void setVelocidadActual(Double velocidadActual){
        this.velocidadActual = velocidadActual;
    }
    public void setTemperaturaActual(Double temperaturaActual){
        this.temperaturaActual = temperaturaActual;
    }

    public void  generarDatosSensor(){
        SensorData sensorData =new SensorData(velocidadActual, temperaturaActual, new Timestamp(System.currentTimeMillis()));
        buffer.agregarDatos(sensorData);
    }

    @Override
    public String toString(){
        return "velocidadActual: "+ this.velocidadActual + "temeperaturaActual: " + this.temperaturaActual;
    }
}

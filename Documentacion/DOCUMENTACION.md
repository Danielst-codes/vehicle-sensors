# DOCUMENTACIÓN DEL SISTEMA: MONITORIZACIÓN Y SEGURIDAD IoT

## 1. Arquitectura de Hardware

El sistema físico está montado sobre una *protoboard* e integrado en un microordenador que actúa como el cerebro computacional (*edge computing*) del vehículo, procesando datos en el origen.

El diseño esquemático, el enrutamiento PCB y la exportación de la lista de materiales han sido modelados utilizando **Fritzing**, estandarizando la documentación visual del circuito.

### 1.1 Lista de Materiales (BOM)

Los componentes físicos interactúan directamente con los pines GPIO de la placa:

- **Microordenador:** Raspberry Pi 5.  
  > **Nota de diseño:** En los esquemáticos de Fritzing se representa utilizando el modelo Raspberry Pi 4 Model B debido a disponibilidad de librerías, pero ambas placas comparten exactamente el mismo pinout GPIO.

- **Sensores de Entorno y Seguridad:**
  - **HC-SR04:** sensor ultrasónico de distancia para detectar objetos y evitar colisiones.
  - **HC-SR501:** sensor de movimiento PIR para identificación de peatones.
  - **DHT11:** sensor de temperatura y humedad para monitorización del clima y sobrecalentamiento del motor.
  - **MLX90614:** sensor de temperatura por infrarrojos conectado vía I2C mediante los pines SDA/SCL.

- **Control de Usuario (Input):**
  - **KY-040:** *rotary encoder* con botón integrado para simular el freno o los controles manuales del usuario.

- **Indicadores Visuales (Output):**
  - Panel de LEDs de estado:
    - Rojo (633 nm)
    - Verde (555 nm)
    - Amarillo (595 nm)
    - Azul (525 nm)
    - Blanco (4500 K)

Estos indicadores permiten alertar visualmente sobre la velocidad, presencia de objetos, detección de peatones y paradas de emergencia.

---

## 2. Arquitectura de Software y Patrones

### 2.1 Programación Orientada a Eventos

El núcleo del sistema abandona el clásico y costoso bucle infinito de comprobación (*polling* pesado) y las peticiones síncronas tradicionales a favor de una **Arquitectura Orientada a Eventos (EDA)**.

#### 2.1.1 Callbacks de Hardware (Python)

En la Raspberry Pi, los sensores físicos están mapeados a funciones de interrupción mediante la librería `gpiozero`. Por ejemplo, el radar de proximidad dispara la emergencia automáticamente al detectar un cambio de estado físico.

```python
# Asignación de eventos asíncronos en el hardware
sensor_distancia.when_in_range = emergencia_objeto
sensor_pir.when_motion = emergencia_peaton
encoder.when_rotated_clockwise = aumentar_velocidad
```

#### 2.1.2 Procesamiento Asíncrono (Java)

A través de RabbitMQ, el backend reacciona a los eventos emitidos por el vehículo. Si se detecta un evento crítico, como por ejemplo un exceso de temperatura, el sistema procesa la alerta sin bloquear el resto de la monitorización.

### 2.2 Topología de Mensajería

El sistema utiliza RabbitMQ como *message broker* central. Se han definido canales de comunicación bidireccional mediante colas específicas.

#### 2.2.1 Cola de Telemetría: `telemetria_coche`

- **Productor:** `MessagesSender` (Python - Raspberry Pi)
- **Consumidor:** `RabbitMQReciver` (Java - Backend)
- **Propósito:** canal por el que el coche envía en tiempo real su estado en formato JSON, incluyendo velocidad, temperatura, humedad y frenos.

#### 2.2.2 Cola de Órdenes: `ordenes_coche`

- **Productor:** `RabbitMQOrdenesSender` (Java - Backend)
- **Consumidor:** módulo de mensajería en Python (**WIP**)
- **Propósito:** canal para emitir comandos de seguridad hacia el vehículo, como por ejemplo forzar el frenado automático.

### 2.3 Gestión de Logs

Para la auditoría y trazabilidad en el backend, se implementa **SLF4J** (*Simple Logging Facade for Java*).

Esta fachada automatiza la creación de logs en el código y trabaja junto con **Logback**, el motor encargado de formatear los registros y volcarlos a un archivo persistente, asegurando el historial de eventos críticos.

---

## 3. Estructura del Proyecto

El código se divide claramente entre la lógica física, el procesamiento backend y la infraestructura.

```text
vehicle-sensors/
├── hardware/               # Entorno Python (Raspberry Pi)
│   ├── core/car.py         # Lógica principal del vehículo y sensores
│   ├── messaging/          # Conexión y envío de JSON a RabbitMQ
│   ├── sensors/            # Controladores físicos (GPIO, I2C)
│   └── main.py             # Script de arranque del productor
│
├── src/main/               # Entorno Java (Backend consumidor)
│   ├── java/               # Código fuente estructurado por dominios
│   │   ├── App.java        # Entry point del servicio
│   │   ├── respuestas/     # Emisor de órdenes al coche
│   │   ├── sensor/         # DTOs para mapear el JSON (SensorData)
│   │   └── telemetria/     # Receptor de RabbitMQ (consumidor)
│   └── resources/          # Archivos de configuración
│       └── logback.xml     # Configuración de logs
│
├── pom.xml                 # Configuración de dependencias Maven
├── target/                 # Binarios generados
│
├── Dockerfile              # Receta de la imagen del backend
├── docker-compose.yml      # Orquestación de RabbitMQ + backend
└── logs/                   # Volumen persistente en el host
    └── telemetria.log      # Historial volcado desde el contenedor
```

---

## 4. Infraestructura y Despliegue

El proyecto utiliza herramientas estándar de la industria para garantizar escalabilidad y replicabilidad.

### 4.1 Compilación y Construcción con Maven

El archivo `pom.xml` define las dependencias principales del proyecto, entre ellas:

- Jackson
- RabbitMQ Client
- Logback

Al empaquetar el proyecto, se genera un **Fat JAR** en la carpeta `target/`, incluyendo el código y sus dependencias listo para producción.

### 4.2 Contenedorización y Persistencia con Docker

#### 4.2.1 Aislamiento

El `Dockerfile` y el `docker-compose.yml` levantan RabbitMQ y el backend en contenedores aislados dentro de su propia red virtual privada.

#### 4.2.2 Persistencia de Datos

Como los contenedores son efímeros, se aplica un **bind mount** para persistir los logs. La ruta interna de logs del backend Java se vincula directamente a la carpeta física `logs/` del sistema anfitrión, evitando la pérdida de datos si el contenedor cae o se reinicia.

---

## 5. Entorno de Desarrollo y Comandos

### 5.1 Pruebas de Hardware

El archivo `test_leds.py` actúa como un *smoke test*. Permite aislar la capa física, encendiendo secuencias de LEDs para validar que los puertos GPIO y las resistencias están correctamente cableados antes de iniciar la lógica compleja.

### 5.2 Entorno Raspberry Pi (Python)

Operando de forma *headless* (sin monitor), se prepara un entorno virtual para no ensuciar el sistema operativo global de la placa.

```bash
# 1. Acceso remoto por SSH
ssh pi@192.168.1.X

# 2. Creación y activación del entorno virtual
python3 -m venv venv
source venv/bin/activate

# 3. Instalación de dependencias
pip install -r requirements.txt
```

### 5.3 Comandos de Despliegue del Backend (Java / Docker)

```bash
# 1. Maven: limpia compilaciones previas y genera el Fat JAR
mvn clean package

# 2. Docker: construye imágenes desde cero y levanta la infraestructura en segundo plano
docker compose up --build -d

# 3. Docker: levanta servicios sin reconstruir la imagen
docker compose up -d
```

---

## 6. Próximas Implementaciones

El diseño modular permite la futura integración de nuevos componentes.

### 6.1 Interfaz Visual

Integración de una pantalla LCD u OLED en el chasis para visualizar la telemetría *on-site*.

### 6.2 Tracción Dinámica

Incorporación de un motor de corriente continua cuyas RPM dependan de las lecturas y eventos del backend, permitiendo simulaciones físicas de aceleración y frenado remoto.
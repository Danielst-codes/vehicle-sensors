import random
import subprocess

puerto_host = random.randint(5000, 9000)

with open("conexion_config.txt", "w") as archivo:
    archivo.write(str(puerto_host))

mapeo = f"{puerto_host}:5672"
subprocess.run(["docker", "rm", "-f", "rabbitmq"])
subprocess.run(["docker", "run", "-d" , "-p", mapeo, "--name", "rabbitmq", "rabbitmq:3-management"])

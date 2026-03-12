from gpiozero import LED
from time import sleep

freno = LED(5)
verde = LED(6)
amarillo = LED(13)
rojo = LED(26)

print("Probando luces...")
while True:
    print("Freno ON")
    freno.on()
    sleep(1)
    freno.off()
    
    print("Verde ON")
    verde.on()
    sleep(1)
    verde.off()
    
    print("Amarillo ON")
    amarillo.on()
    sleep(1)
    amarillo.off()
    
    print("Rojo ON")
    rojo.on()
    sleep(1)
    rojo.off()
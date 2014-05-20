'''
A silly python program to test the daily-voc app.
'''

import serial
import time

serial = serial.Serial('/dev/ttyUSB0', 9600, timeout=1)
ack = '\x01\x01\x04\x0c\x44\x96\x60\x00\x41\xc7\x33\x33\x41\x8c\x00\x00\x22\x53'

while True:
    while serial.inWaiting() < 1:
        continue
    time.sleep(0.1)
    r = serial.read(8)
    serial.flushOutput()
    s = 'Rx: ' + ' '.join([hex(ord(i)) for i in r]).replace('0x', '')
    print s

    time.sleep(0.2)
    s = 'Tx: ' + ' '.join([hex(ord(i)) for i in ack]).replace('0x', '')
    print s
    serial.write(ack)

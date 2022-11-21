# GPS publisher 코드 

import serial,time
import json
import requests

port = '/dev/ttyACM0'
baud = 9600
ID="MFBE29"

from math import sin, cos, sqrt, atan2, radians

def lat_long_dist(lat1,lon1,lat2,lon2):
    # function for calculating ground distance between two lat-long locations
    R = 6373.0 # approximate radius of earth in km. 
    lat1 = radians( float(lat1) )
    lon1 = radians( float(lon1) )
    lat2 = radians( float(lat2) )
    lon2 = radians( float(lon2) )
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2)**2 + cos(lat1) * cos(lat2) * sin(dlon / 2)**2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    distance = round(R * c, 6)
    return distance

serialPort = serial.Serial(port, baudrate = baud, timeout = 0.5)

#mobious datasend
def send_mobious(gpsdata):  
	url = "http://YOURIP/Mobius/kick_off/data/gps"

	payload = "{\n    \"m2m:cin\": {\n        \"con\": \"%s\"\n    }\n}"%(gpsdata)
	headers = {
	'Accept': 'application/json',
	'X-M2M-RI': '12345',
	'X-M2M-Origin': '{{aei}}',
	'Content-Type': 'application/vnd.onem2m-res+json; ty=4'
	}

	response = requests.request("POST", url, headers=headers, data=payload)

	print(response.text)
 
la1=0
lo1=0

while True:
    
    str = ''
    try:
        str = serialPort.readline().decode().strip()
    except Exception as e:
        print(e)

    
    if str.find('GGA') > 0:
        try:
            msg = pynmea2.parse(str)
            Lat=round(msg.latitude,6)
            Lon=round(msg.longitude,6)
            Alt=msg.altitude
            Sats=msg.num_sats
            speed=lat_long_dist(la1,lo1,float(round(msg.latitude,6)),float(round(msg.longitude,6)))

            la1=Lat
            lo1=Lon
            if speed>40:
                speed=0
            # print('Lat:',round(msg.latitude,6),'Lon:',round(msg.longitude,6),'Alt:',msg.altitude,'Sats:',msg.num_sats,'speed:',speed)
            
            data=ID+" "+"%.6f "%Lat+"%.6f " %Lon+"%.6f "%speed    # +"%C "%Alt+"%f "%Sats
            send_mobious(data)
            
        except Exception as e:
            print(e)
    time.sleep(0.1)
    
client.disconnect()
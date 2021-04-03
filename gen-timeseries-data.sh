mqtt_host=localhost
mqtt_port=1883
mqtt_topic=device-stats

echo "message format - <location>,<device>,<temperature>,<pressure>"

while true; do
   for loc in $(seq 1 5); do #5 locations
      for device in $(seq 1 5); do #5 devices per location
         #temp=$(jot -r 1 20 50)
         temp=$(jot -r 1) #erratic!
         pressure=$(jot -r 1 51 100)

         #msg=`echo temp,$temp,device$device,loc$loc`;
         msg=`echo $loc,$device,$temp,$pressure`;
         $(mosquitto_pub -h ${mqtt_host} -t ${mqtt_topic} -q 2 -m "${msg}");
         echo $msg
         #msg=`echo pressure,$pressure,device$device,loc$loc`;
         #$(mosquitto_pub -h ${mqtt_host} -p ${mqtt_port} -t ${mqtt_topic} -q 2 -m "${msg}");

         #sleep 0.5s
      done
   done
done
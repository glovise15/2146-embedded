package com.mapr.demo.mqtt.simple;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SimpleMqttCallBack implements MqttCallback {
	Socket socket;
	boolean gateway;
	boolean subscribersPresent;

	public SimpleMqttCallBack(Socket socket) {
		this.socket = socket;
		this.gateway = true;
	}

	public SimpleMqttCallBack() {
	}

	public void connectionLost(Throwable throwable) {
		System.out.println("Connection to MQTT broker lost!");
	}

	// This method is called whenever a message is received
	public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
		// If this is a subscriber callback then just print the payload
		if (!gateway){
			System.out.println("Message received:\t"+ new String(mqttMessage.getPayload()) );
			return;
		}
		// Else this is a gateway callback
		try {
			// Read the number of subcribers received from the mosquitto broker
			Writer out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			Scanner scanner = new Scanner(System.in);
			String msg = new String(mqttMessage.getPayload());
			
			// If there no subscribers, tell the root to stop sending data
			if(Integer.parseInt(msg) <= 1){
				subscribersPresent = false;
				out.write(2 + "\0");
				out.flush();
			// Else if we went from 0 subscribers to 1 or more, notifies the root that it can start sending data
			}else if (!subscribersPresent){
				subscribersPresent = true;				
				out.write(0 + "\0");
				out.flush();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Message received:\t" + new String(mqttMessage.getPayload()));
	}

	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
	}
}


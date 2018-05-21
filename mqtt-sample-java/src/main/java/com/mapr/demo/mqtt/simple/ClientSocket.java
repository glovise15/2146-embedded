package com.mapr.demo.mqtt.simple;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.util.Debug;

public class ClientSocket {

	public static void main(String[] args) {
		String hostname = "localhost";
		int port = 60001;
		setup(hostname, port);

	}

	public static void setup(String hostname, int port) {
		try {
			final Socket socket = new Socket(hostname, port);
			final MqttClient client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId());
    			client.setCallback( new SimpleMqttCallBack(socket) );
			client.connect();
			client.subscribe("$SYS/broker/subscriptions/count");
			
			Thread receiver = new Thread(new Runnable() {

				@Override
				public void run() {

					InputStream input = null;
					try {
						input = socket.getInputStream();
						InputStreamReader reader = new InputStreamReader(input);
						int character;
						while (true) {
							StringBuilder data = new StringBuilder();
							while ((character = reader.read()) != -1 && character != '\n' && character != '\0') {
								data.append((char) character);
							}
							String msg = data.toString();
							if (msg.contains("./")) {
								String[] split = msg.split(" |./");
								String topic = split[1];
								String value = split[2];
								mqttPublish(client, topic, value);
							}
						}
					} catch (IOException | MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});

			Thread sender = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Writer out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
						Scanner scanner = new Scanner(System.in);
						while (true) {
							String msg = scanner.nextLine();
							out.write(msg + "\0");
							out.flush();
						}

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			receiver.start();
			sender.start();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void mqttPublish(MqttClient client, String topic, String value) throws MqttException {
		System.out.println("== START PUBLISHER ==");
		System.out.println(topic + " "+ value);
		MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setPayload(value.getBytes());
		client.publish(topic, mqttMessage);
		System.out.println("\tMessage '" + value + "' to "+topic);

	}
}

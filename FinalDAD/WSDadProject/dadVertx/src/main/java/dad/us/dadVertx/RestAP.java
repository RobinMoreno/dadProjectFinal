package dad.us.dadVertx;

import java.util.ArrayList;
import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttPublishMessage;

public class RestAP extends AbstractVerticle {

	private SQLClient mySQLClient;
	private static Multimap<String, MqttEndpoint> clientTopics;
	private String IP = "192.168.43.66";

	public void start(Future<Void> startFuture) {

		JsonObject mySQlClientConfig = new JsonObject().put("host", "127.0.0.1").put("port", 3306)
				.put("database", "daddevices").put("username", "root").put("password", "root");

		mySQLClient = MySQLClient.createShared(vertx, mySQlClientConfig);
		
		Router router = Router.router(vertx);
		router.route().handler(StaticHandler.create("webroot").setCachingEnabled(false));
		vertx.createHttpServer().requestHandler(router::accept).listen(80, res -> {

			if (res.succeeded()) {
				System.out.println("Servidor desplegado");
			} else {
				System.out.println("Error: " + res.cause());
			}
		});
		
		router.route("/api/devices").handler(BodyHandler.create());
		router.route("/api/reading").handler(BodyHandler.create());
		router.route("/api/reading/smoke").handler(BodyHandler.create());
		router.route("/api/mqtt").handler(BodyHandler.create());
		
		router.get("/api/devices").handler(this::getAll);
		router.get("/api/devices/:idFilter").handler(this::getOne);
		router.get("/api/reading/:idFilter").handler(this::getOneReading);
		router.get("/api/reading/mqtttemperature)").handler(this::getMqttUpdateTemp);
		router.get("/api/reading/mqttsmoke)").handler(this::getMqttUpdateSmoke);
		
		router.put("/api/devices").handler(this::addOne);
		router.delete("/api/devices").handler(this::deleteOne);
		
		router.post("/api/devices/off").handler(this::offState);
		router.post("/api/devices/on").handler(this::onState);
		router.post("/api/devices/temperature/:id/:temp").handler(this::updateTemperature);
		router.post("/api/devices/:idFilter").handler(this::updateOneRead);
		router.post("/api/devices/:id/:state").handler(this::updateState);
		router.post("/api/reading").handler(this::updateTempRead);
		router.post("/api/reading/smoke").handler(this::updateSmokeRead);
		
		router.get("/api/mqtt/turnOn").handler(this::getMqttTurnOn);
		router.get("/api/mqtt/turnOff").handler(this::getMqttTurnOff);

		
		clientTopics = HashMultimap.create();

		MqttServer mqttServer = MqttServer.create(vertx);
		init(mqttServer);

		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient.connect(1883, "localhost", s -> {
			
			mqttClient.subscribe("Lecturas", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal Lecturas");
				}
			});
			
//			new Timer().scheduleAtFixedRate(new TimerTask() {
//				
//				@Override
//				public void run() {
//					
//					mqttClient.publish("Lecturas", Buffer.buffer("pene"), MqttQoS.AT_LEAST_ONCE, false, false);
//				}
//			}, 1000, 3000);
		});

		MqttClient mqttClient2 = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient2.connect(1883, "localhost", s -> {

			mqttClient2.subscribe("Lecturas", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal Lecturas");
					
					mqttClient2.publishHandler(new Handler<MqttPublishMessage>() {
						@Override
						public void handle(MqttPublishMessage arg0) {
							
							System.out.println("Mensaje recibido por el cliente 2: " + arg0.payload().toString());							
						}
					});
				}
			});

		});

	}

	private static void init(MqttServer mqttServer) {
		mqttServer.endpointHandler(endpoint -> {
			System.out.println("Nuevo cliente MQTT [" + endpoint.clientIdentifier()
					+ "] solicitando suscribirse [Id de sesión: " + endpoint.isCleanSession() + "]");
			// Indicamos al cliente que se ha contectado al servidor MQTT y que no tenía
			// sesión previamente creada (parámetro false)
			endpoint.accept(false);

			// Handler para gestionar las suscripciones a un determinado topic
			handleSubscription(endpoint);

			// Handler para gestionar las desuscripciones de un determinado topic
			handleUnsubscription(endpoint);

			// Este handler será llamado cuando se realice una llamada PUBLISH por parte del
			// cliente
			publishHandler(endpoint);

			// Handler encargado de gestionar las desconexiones de los clientes al servidor
			handleClientDisconnect(endpoint);
		}).listen(ar -> {
			if (ar.succeeded()) {
				System.out.println("MQTT server está a la escucha por el puerto " + ar.result().actualPort());
			} else {
				System.out.println("Error desplegando el MQTT server");
				ar.cause().printStackTrace();
			}
		});
	}

	private static void handleSubscription(MqttEndpoint endpoint) {
		endpoint.subscribeHandler(subscribe -> {
			
			List<MqttQoS> grantedQosLevels = new ArrayList<>();
			for (MqttTopicSubscription s : subscribe.topicSubscriptions()) {
				System.out.println("Suscripci�n al topic " + s.topicName() + " con QoS " + s.qualityOfService());
				grantedQosLevels.add(s.qualityOfService());
				clientTopics.put(s.topicName(), endpoint);
			}
			endpoint.subscribeAcknowledge(subscribe.messageId(), grantedQosLevels);
		});
	}

	private static void handleUnsubscription(MqttEndpoint endpoint) {
		endpoint.unsubscribeHandler(unsubscribe -> {
			for (String t : unsubscribe.topics()) {
				clientTopics.remove(t, endpoint);
				System.out.println("Eliminada la suscripci�n del topic " + t);
			}
			endpoint.unsubscribeAcknowledge(unsubscribe.messageId());
		});
	}

	private static void publishHandler(MqttEndpoint endpoint) {
		endpoint.publishHandler(message -> {
			handleMessage(message, endpoint);
		}).publishReleaseHandler(messageId -> {
			endpoint.publishComplete(messageId);
		});
	}

	private static void handleMessage(MqttPublishMessage message, MqttEndpoint endpoint) {
		System.out.println("Mensaje publicado por el cliente " + endpoint.clientIdentifier() + " en el topic "
				+ message.topicName());
		System.out.println("    Contenido del mensaje: " + message.payload().toString());
		
		System.out.println("Origen: " + endpoint.clientIdentifier());
		for (MqttEndpoint client: clientTopics.get(message.topicName())) {
			System.out.println("Destino: " + client.clientIdentifier());
			if (!client.clientIdentifier().equals(endpoint.clientIdentifier()))
				client.publish(message.topicName(), message.payload(), message.qosLevel(), message.isDup(), message.isRetain());
		}
		
		if (message.qosLevel() == MqttQoS.AT_LEAST_ONCE) {
			String topicName = message.topicName();
			switch (topicName) {
			}
			
			endpoint.publishAcknowledge(message.messageId());
		} else if (message.qosLevel() == MqttQoS.EXACTLY_ONCE) {
			
			endpoint.publishRelease(message.messageId());
		}
	}
	
	private static void handleClientDisconnect(MqttEndpoint endpoint) {
		endpoint.disconnectHandler(h -> {
			// Eliminamos al cliente de todos los topics a los que estaba suscritos
			Stream.of(clientTopics.keySet())
				.filter(e -> clientTopics.containsEntry(e, endpoint))
				.forEach(s -> clientTopics.remove(s, endpoint));
			System.out.println("El cliente remoto se ha desconectado [" + endpoint.clientIdentifier() + "]");
		});
	}

	private void getMqttUpdateTemp(RoutingContext routingContext) {
		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient.connect(1883, "192.168.43.66", s -> {
			
			
			mqttClient.subscribe("Lecturas", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal Lecturas");
				}
			});
			mqttClient.publish("Lecturas", Buffer.buffer("temperature"), MqttQoS.AT_LEAST_ONCE, false, false);
			routingContext.response().setStatusCode(200).end();
		});
	}
	
	private void getMqttUpdateSmoke(RoutingContext routingContext) {
		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient.connect(1883, "192.168.43.66", s -> {
			
			
			mqttClient.subscribe("Lecturas", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal Lecturas");
				}
			});
			mqttClient.publish("Lecturas", Buffer.buffer("smoke"), MqttQoS.AT_LEAST_ONCE, false, false);
			routingContext.response().setStatusCode(200).end();
		});
	}
	
	private void getOne(RoutingContext routingContext) {

		String paramStr = routingContext.request().getParam("idFilter");

		if (paramStr != null) {
			try {
				int param = Integer.parseInt(paramStr);
				mySQLClient.getConnection(conn -> {
					if (conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "SELECT id, name, temperature, state FROM daddevices.devices WHERE id = ?";
						JsonArray paramQuery = new JsonArray().add(param);
						connection.queryWithParams(query, paramQuery, res -> {
							connection.close();
							conn.result().close();
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows().get(0)));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} else {
						routingContext.response().setStatusCode(400).end("Error" + conn.cause());
					}
				});
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	private void getOneReading(RoutingContext routingContext) {

		String paramStr = routingContext.request().getParam("idFilter");

		if (paramStr != null) {
			try {
				int param = Integer.parseInt(paramStr);
				mySQLClient.getConnection(conn -> {
					if (conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "SELECT * FROM daddevices.reading WHERE iddevice = ?";
						JsonArray paramQuery = new JsonArray().add(param);
						connection.queryWithParams(query, paramQuery, res -> {
							connection.close();
							conn.result().close();
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows().get(0)));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} else {
						routingContext.response().setStatusCode(400).end("Error" + conn.cause());
					}
				});
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}

	private void getAll(RoutingContext routingContext) {

		String paramStr = routingContext.request().getParam("idFilter");

		if (paramStr != null) {
			try {
				int param = Integer.parseInt(paramStr);
				mySQLClient.getConnection(conn -> {
					if (conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "SELECT * FROM daddevices.devices";
						JsonArray paramQuery = new JsonArray().add(param);
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} else {
						routingContext.response().setStatusCode(400).end("Error" + conn.cause());
					}
				});
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}

	private void offState(RoutingContext routingContext) {

		String paramStr = routingContext.request().getParam("id");

		if (paramStr != null) {
			try {
				DeviceCustom d = Json.decodeValue(routingContext.getBodyAsString(), DeviceCustom.class);
				
;				mySQLClient.getConnection(conn -> {
					if (conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "UPDATE daddevices.devices SET state = 0 WHERE id = 1";
						JsonArray paramQuery = new JsonArray().add(d.getId());
						connection.queryWithParams(query, paramQuery, res -> {
							conn.result().close();
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} else {
						routingContext.response().setStatusCode(400).end("Error" + conn.cause());
					}
				});
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	private void onState(RoutingContext routingContext) {

		String paramStr = routingContext.request().getParam("id");

		if (paramStr != null) {
			try {
				DeviceCustom d = Json.decodeValue(routingContext.getBodyAsString(), DeviceCustom.class);
				
;				mySQLClient.getConnection(conn -> {
					if (conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "UPDATE daddevices.devices SET state = 1 WHERE id = 1";
						JsonArray paramQuery = new JsonArray().add(d.getId());
						connection.queryWithParams(query, paramQuery, res -> {
							conn.result().close();
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} else {
						routingContext.response().setStatusCode(400).end("Error" + conn.cause());
					}
				});
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	private void updateState(RoutingContext routingContext) {

		String paramStr = routingContext.request().getParam("id");

		if (paramStr != null) {
			try {
				int id = Integer.parseInt(paramStr);
				int state = Integer.parseInt(routingContext.request().getParam("state"));
				
;				mySQLClient.getConnection(conn -> {
					if (conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "UPDATE daddevices.devices SET state = ? WHERE id = ?";
						JsonArray paramQuery = new JsonArray().add(state).add(id);
						connection.queryWithParams(query, paramQuery, res -> {
							conn.result().close();
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} else {
						routingContext.response().setStatusCode(400).end("Error" + conn.cause());
					}
				});
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}	
	
	private void updateTemperature(RoutingContext routingContext) {

		String paramStr = routingContext.request().getParam("id");
		//String paramTemp = routingContext.request().getParam("temperature");

		if (paramStr != null) {
			try {
				int id = Integer.parseInt(paramStr);
				//double temp = Double.parseDouble(paramTemp);
				double temp = Double.parseDouble(routingContext.request().getParam("temp"));
				
;				mySQLClient.getConnection(conn -> {
					if (conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "UPDATE daddevices.devices SET temperature = ? WHERE id = ?";
						JsonArray paramQuery = new JsonArray().add(temp).add(id);
						connection.queryWithParams(query, paramQuery, res -> {
							conn.result().close();
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} else {
						routingContext.response().setStatusCode(400).end("Error" + conn.cause());
					}
				});
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	private void updateOneRead(RoutingContext routingContext) {

		String paramStr = routingContext.request().getParam("idFilter");

		if (paramStr != null) {
			try {
				int param = Integer.parseInt(paramStr);
				mySQLClient.getConnection(conn -> {
					if (conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "UPDATE daddevices.devices SET state = 1 WHERE id = ?";
						JsonArray paramQuery = new JsonArray().add(param);
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} else {
						routingContext.response().setStatusCode(400).end("Error" + conn.cause());
					}
				});
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	private void updateTempRead(RoutingContext routingContext) {

		Reading state = Json.decodeValue(routingContext.getBodyAsString(), Reading.class);

		if (state != null) {
			try {
				mySQLClient.getConnection(conn -> {
					if (conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "UPDATE daddevices.reading SET temperature = ? WHERE iddevice = ?";
						JsonArray paramQuery = new JsonArray().add(state.getTemperature()).add(state.getIddevice());
						connection.queryWithParams(query, paramQuery, res -> {
							conn.result().close();
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} else {
						routingContext.response().setStatusCode(400).end("Error" + conn.cause());
					}
				});
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	private void updateSmokeRead(RoutingContext routingContext) {

		Reading state = Json.decodeValue(routingContext.getBodyAsString(), Reading.class);

		if (state != null) {
			try {
				mySQLClient.getConnection(conn -> {
					if (conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "UPDATE daddevices.reading SET smoke = ? WHERE iddevice = ?";
						JsonArray paramQuery = new JsonArray().add(state.getSmoke()).add(state.getIddevice());
						connection.queryWithParams(query, paramQuery, res -> {
							conn.result().close();
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} else {
						routingContext.response().setStatusCode(400).end("Error" + conn.cause());
					}
				});
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}

	private void deleteOne(RoutingContext routingContext) {

		String paramStr = routingContext.request().getParam("idFilter");

		if (paramStr != null) {
			try {
				int param = Integer.parseInt(paramStr);
				mySQLClient.getConnection(conn -> {
					if (conn.succeeded()) {

						SQLConnection connection = conn.result();
						String query = "DELETE FROM daddevices.devices" + "WHERE id = ?";
						JsonArray paramQuery = new JsonArray().add(param);
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} else {
						routingContext.response().setStatusCode(400).end("Error" + conn.cause());
					}
				});
			} catch (ClassCastException e) {
				routingContext.response().setStatusCode(400).end();
			}
		} else {
			routingContext.response().setStatusCode(400).end();
		}
	}

	private void addOne(RoutingContext routingContext) {
		try {
			mySQLClient.getConnection(conn -> {
				if (conn.succeeded()) {
					try {
						DeviceCustom d = Json.decodeValue(routingContext.getBodyAsString(), DeviceCustom.class);
						SQLConnection connection = conn.result();
						String query = "INSERT INTO daddevices.devices (id, name, state, temperature, smoke) VALUES (DEFAULT,?,?,?,?)";
						JsonArray paramQuery = new JsonArray().add(d.getId());// TODO
						connection.queryWithParams(query, paramQuery, res -> {
							if (res.succeeded()) {
								routingContext.response().end(Json.encodePrettily(res.result().getRows()));
							} else {
								routingContext.response().setStatusCode(400).end(conn.cause().toString());
							}
						});
					} catch (Exception e) {
						System.out.println(e.toString());
					}
				} else {
					routingContext.response().setStatusCode(400).end("Error" + conn.cause());
				}
			});
		} catch (ClassCastException e) {
			routingContext.response().setStatusCode(400).end();
		}
	}
	
	private void getMqttTurnOn(RoutingContext routingContext) {
		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient.connect(1883, IP, s -> {
			
			
			mqttClient.subscribe("Lecturas", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal Lecturas");
				}
			});
			mqttClient.publish("Lecturas", Buffer.buffer("turnOnn"), MqttQoS.AT_LEAST_ONCE, false, false);
			routingContext.response().setStatusCode(200).end();
		});
	}
	
	private void getMqttTurnOff(RoutingContext routingContext) {
		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient.connect(1883, IP, s -> {
			
			
			mqttClient.subscribe("Lecturas", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					
					System.out.println("Cliente " + mqttClient.clientId() + " suscrito correctamente al canal Lecturas");
				}
			});
			mqttClient.publish("Lecturas", Buffer.buffer("turnOff"), MqttQoS.AT_LEAST_ONCE, false, false);
			routingContext.response().setStatusCode(200).end();
		});
	}
}

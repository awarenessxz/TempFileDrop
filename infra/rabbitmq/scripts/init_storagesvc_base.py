#!/usr/bin/env python
import pika

rabbitmq_host = "127.0.0.1"
rabbitmq_user = "admin"
rabbitmq_password = "admin123"
rabbitmq_port = 5672
rabbitmq_virtual_host = "/"
rabbitmq_exchange = "storageSvcExchange"
rabbitmq_rcv_queue = "storageSvcExchange.tempfiledrop"	# change this <QUEUE_NAME>.<CONSUMER_GROUP>
rabbitmq_rcv_key = "tempfiledrop"			# change this

# connect to rabbitmq
credentials = pika.PlainCredentials(rabbitmq_user, rabbitmq_password)
parameters = pika.ConnectionParameters(rabbitmq_host, rabbitmq_port, rabbitmq_virtual_host, credentials)
connection = pika.BlockingConnection(parameters)
channel = connection.channel()

# declare exchange
channel.exchange_declare(exchange=rabbitmq_exchange, exchange_type='topic', durable=True, auto_delete=True)

# declare queue
channel.queue_declare(queue=rabbitmq_rcv_queue)

# bind queue to exchange
channel.queue_bind(exchange=rabbitmq_exchange, queue=rabbitmq_rcv_queue, routing_key=rabbitmq_rcv_key)
channel.queue_bind(exchange=rabbitmq_exchange, queue=rabbitmq_rcv_queue, routing_key="KEY2")

#outgoingRoutingKeys = ["tempfiledrop"]
#outgoingQueues = ["storageSvcChannel"]
#for index in range(len(outgoingRoutingKeys)):
#    channel.queue_bind(exchange=rabbitmq_send_exchange, queue=outgoingQueues[index], routing_key=outgoingRoutingKeys[index])





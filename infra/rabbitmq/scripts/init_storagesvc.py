#!/usr/bin/python

import getopt, sys, pika

# tell getopts() about which parameters are valid
# Eg. 
# short_options = "ho:v"
# long_options = ["help", "output=", "verbose"]
# ------------------------------------------
# long argument   short argument  with value
# ------------------------------------------
# --help           -h              no
# --output         -o              yes
# --verbose        -v              no
# ------------------------------------------
#
# Note: o: means that o should be assigned an argument

#######################
rabbitmq_host = "127.0.0.1"
rabbitmq_user = "admin"
rabbitmq_password = "admin123"
rabbitmq_port = 5672
rabbitmq_virtual_host = "/"
#######################
rabbitmq_exchange = None
rabbitmq_rcv_queue = None
rabbitmq_rcv_keys = []
create_exchange = False
create_queue = False
bind_queue = False
channel = None

def usage():
    print("init_storagesvc.py --create-exchange -e <EXCHANGE_NAME>")
    print("init_storagesvc.py --create-queue -q <QUEUE_NAME>")
    print("init_storagesvc.py --bind-queue -q <QUEUE_NAME> -e <EXCHANGE_NAME> -r <ROUTER_KEY1>,<ROUTER_KEY2>")
    sys.exit(0)

def declare_exchange():
    if create_exchange:
        print("Declaring Exchange --- {}".format(rabbitmq_exchange))
        channel.exchange_declare(exchange=rabbitmq_exchange, exchange_type='topic', durable=True, auto_delete=False)

def declare_queue():
    if create_queue:
        print("Declaring Queue --- {}".format(rabbitmq_rcv_queue))
        channel.queue_declare(queue=rabbitmq_rcv_queue)

def bind_queue_to_exchange():
    if bind_queue:
        print("Binding Queue ({}) to Exchange ({})".format(rabbitmq_rcv_queue, rabbitmq_exchange))
        for key in rabbitmq_rcv_keys:
            nkey = key.strip()
            if nkey:
                channel.queue_bind(exchange=rabbitmq_exchange, queue=rabbitmq_rcv_queue, routing_key=nkey)

def connect_to_rabbitmq():
    global channel
    print("connecting to rabbitmq....")
    credentials = pika.PlainCredentials(rabbitmq_user, rabbitmq_password)
    parameters = pika.ConnectionParameters(rabbitmq_host, rabbitmq_port, rabbitmq_virtual_host, credentials)
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

def process():
    print("Arguments ==> {}".format(rabbitmq_exchange, rabbitmq_rcv_queue, rabbitmq_rcv_keys, create_exchange, create_queue, bind_queue))
    connect_to_rabbitmq()
    declare_exchange()
    declare_queue()
    bind_queue_to_exchange()

def validate():
    if not create_exchange and not create_queue and not bind_queue:
        print("Please specify a command...\n")
        usage()
    if create_exchange:
        if rabbitmq_exchange == None or not rabbitmq_exchange:
            print("Please provide exchange...\n")
            usage()
    if create_queue:
        if rabbitmq_rcv_queue == None or not rabbitmq_rcv_queue:
            print("Please provide queue...\n")
            usage()
    if bind_queue:
        if rabbitmq_exchange == None or not rabbitmq_exchange or rabbitmq_rcv_queue == None or not rabbitmq_rcv_queue or rabbitmq_rcv_keys == None or not rabbitmq_rcv_keys:
            print("Please provide exchange, queue and router keys...\n")
            usage()
    process()

def main(argv):
    arguments = len(sys.argv) - 1
    if (arguments <= 0):
        usage()

    try:
        short_options = "hq:e:r:"
        long_options = ["help", "create-exchange", "create-queue", "bind-queue"]
        opts, args = getopt.getopt(argv, short_options, long_options)
    except getopt.GetoptError:
        print("Invalid options...\n")
        usage()

    global rabbitmq_exchange, rabbitmq_rcv_queue, rabbitmq_rcv_keys, create_exchange, create_queue, bind_queue

    for opt, arg in opts:
        if opt == '-q':
            rabbitmq_rcv_queue = arg
        elif opt == '-e':
            rabbitmq_exchange = arg
        elif opt == '-r':
            rabbitmq_rcv_keys = arg.strip().split(",")
        elif opt in ("-h", "--help"):
            usage()
        elif opt == "--create-exchange":
            create_exchange = True
        elif opt == "--create-queue":
            create_queue = True
        elif opt == "--bind-queue":
            bind_queue = True

    validate()

if __name__ == "__main__":
    main(sys.argv[1:])

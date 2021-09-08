printf "\n==================================================\n"
printf "Initializing Minio Notification....\n"
printf "==================================================\n"
mc alias set local http://localhost:9000 minio minio123
mc admin config set local/ notify_amqp:1 exchange="minioBucketEvents" exchange_type="fanout" mandatory="off" no_wait="off" url="amqp://minio_admin:minio123@rabbitmq:5672" auto_deleted="off" delivery_mode="0" durable="on" internal="off" routing_key="bucketlogs"
mc admin service restart local/
mc mb local/tempfiledrop
mc event add local/tempfiledrop arn:minio:sqs::1:amqp
mc event list local/tempfiledrop
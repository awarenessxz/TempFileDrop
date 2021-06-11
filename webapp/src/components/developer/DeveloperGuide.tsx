import React from "react";
import ConsumerImage from "../../assets/consumer.jpg";
import RolesImage from "../../assets/keycloak_roles.png";

const DeveloperGuide = (): JSX.Element => {
    return (
        <div className="developer-content">
            <h2>Overview</h2>
            <p>
                Centralized Storage Service is a microservice for uploading, downloading and storing of files.
                The primary storage medium is based on <b>object storage</b> using a <b>MinIO Cluster</b>.
                There are a couple of key features available such as <b>Scheduled clean up</b>, <b>Event Feedback </b>
                and <b>Anonymous Uploads</b>. Let us worry about the storage problems like availability, integrity
                and security while you focus on your development!
            </p>
            <div className="img-container">
                <img alt="consumer" src={ConsumerImage} />
            </div>
            <h2>Usage</h2>
            <p>
                This section covers information you will need in order to consume the Centralized Storage
                Service. You will need the follow things which will mostly be provided by the Administrator.
            </p>
            <ul>
                <li>Dedicated <b>Storage Bucket</b> for storing your files</li>
                <li>Register your applications/services with Keycloak and tag <b>roles</b> for your
                    users so that you have <b>Bearer Token</b> available in your Http Request Headers</li>
                <li>Dedicated <b>Message Queue</b> & <b>Routing Key</b> for you to subscribe to get
                    upload/downloaded/deleted events</li>
            </ul>
            <h2>Example</h2>
            <p>
                Put up a request to the administrator to consume the Centralized Storage Service and you
                will receive the following properties which you will have to apply into your applications
                and services.
            </p>
            <ul>
                <li><b>Bucket Name</b>: SampleApp</li>
                <li><b>Keycloak Configuration</b>: keycloak.json / realm & client roles</li>
                <li><b>RabbitMQ Configuration</b>: exchange / queue / routing key / url / credentials</li>
            </ul>
            <h4>1) Connect to Keycloak</h4>
            <p>
                The Centralized Storage Service uses Keycloak to <b>authenticate requests using bearer tokens.</b>
                To consume the Centralized Storage Service, you will probably need to authenticate users
                through keycloak to obtain the bearer token. Hence, when you put up a request to the
                administrator, the administrator will register your applications/services as a <b>client</b>.
                The diagram below is an example of how roles will be tagged to your users.
            </p>
            <div className="img-container">
                <img alt="keycloak roles" src={RolesImage} />
            </div>
            <p>When users are authenticated, they will receive a bearer token which will authorized the
                user to consume the Centralized Storage Service. This bearer token will have to be attached
                to the http request (refer below).</p>
            <h5>Configuration</h5>
            <p>Your application will be registered as a <b>Client</b> on keycloak. To authenticate with
                keycloak, use the following keycloak configuration for your client. Replace the values
                in <b>[ ]</b> brackets with the values you were given.
            </p>
            <ul>
                <li> application.yaml (Spring Boot Restful Web Microservices)
                    <pre>
                        <code>
                            keycloak:<br/>
                            &ensp;&ensp;&ensp;&ensp;auth-server-url: [KEYCLOAK_AUTH_URL]<br/>
                            &ensp;&ensp;&ensp;&ensp;realm: storage<br/>
                            &ensp;&ensp;&ensp;&ensp;resource: [YOUR_APP_CLIENT_NAME]<br/>
                            &ensp;&ensp;&ensp;&ensp;principal-attribute: preferred_username<br/>
                            &ensp;&ensp;&ensp;&ensp;bearer-only: true<br/>
                            &ensp;&ensp;&ensp;&ensp;public-client: false<br/>
                            &ensp;&ensp;&ensp;&ensp;use-resource-role-mappings: true<br/>
                        </code>
                    </pre>
                </li>
            </ul>
            <h4>2) Connect to RabbitMQ</h4>
            <p>
                If you would like to receive upload/delete/download events from Centralized Storage Service. You will
                need to subscribe to the <b>dedicated queue and routing key</b> created for you by the administrator.
                You will be given a <b>service account</b> to login to the rabbitMQ. However, do note that this service account
                only has <b>read</b> permissions. Replace the values in <b>[ ]</b> brackets with the values you were given.
            </p>
            <ul>
                <li><b>RabbitMQ Endpoints</b>: [RABBITMQ_ENDPOINTS]</li>
                <li><b>Service Account username</b>: [RABBITMQ_SUBSCRIBER_USER]</li>
                <li><b>Service Account password</b>: [RABBITMQ_SUBSCRIBER_PASSWORD]</li>
                <li><b>Exchange Name</b>: storageSvcExchange</li>
                <li><b>Queue Name</b>: [YOUR_APP_QUEUE_NAME]</li>
                <li><b>Routing Key</b>: [YOUR_APP_ROUTING_KEY]</li>
            </ul>
            <p>
                Do take note that in order for you to receive the events, you will have to ensure that you include the
                <b> routing key</b> when you upload/delete/download files from the Centralized Storage Service. Also,
                below is an example of the event message that you will receive.
            </p>
            <pre>
                <code>
                    data class EventMessage(<br/>
                    &ensp;&ensp;&ensp;&ensp;val eventType: String, /* FILES_UPLOADED || FILES_DOWNLOADED || FILES_DELETED */<br/>
                    &ensp;&ensp;&ensp;&ensp;val storageId: String,<br/>
                    &ensp;&ensp;&ensp;&ensp;val storagePath: String,<br/>
                    &ensp;&ensp;&ensp;&ensp;val storageFiles: String,<br/>
                    &ensp;&ensp;&ensp;&ensp;val bucket: String,<br/>
                    &ensp;&ensp;&ensp;&ensp;val data: String<br/>
                    )
                </code>
            </pre>
            <h4>3) Configure Http Request for Upload / Download / Delete</h4>
            <p>
                In order to perform any sorts of http requests to Centralized Storage Service, you will have to ensure that
                you include the <b>Bearer Token</b> in the http request header. Also, ensure that you provide the <b>routing key</b>
                in your http request if you want to receive an event feedback. For more information on the rest endpoints,
                refer to the swagger API documentation.
            </p>
            <pre>
                <code>
                    request.headers['Authorization'] = 'Bearer ' + token;
                </code>
            </pre>
        </div>
    );
};

export default DeveloperGuide;

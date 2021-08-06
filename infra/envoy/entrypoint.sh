#!/bin/bash

#####################################################################################################
# Setting up Default Values
####################################################################################################

KEYCLOAK_REALM=${KEYCLOAK_REALM:-storage}
KEYCLOAK_PROTOCOL=${KEYCLOAK_PROTOCOL:-https}
KEYCLOAK_PORT=${KEYCLOAK_PORT:-443}
if [ $KEYCLOAK_PORT = 443 ] || [ KEYCLOAK_PORT = 80 ]; then
    KEYCLOAK_URL="$KEYCLOAK_PROTOCOL://$KEYCLOAK_HOST"
else
    KEYCLOAK_URL="$KEYCLOAK_PROTOCOL://$KEYCLOAK_HOST:$KEYCLOAK_PORT"
fi
OPA_HOST=${OPA_HOST:-opa}
OPA_PORT=${OPA_PORT:-9191}
TIMEOUT=${TIMEOUT:-15s}
ENVOY_PORT=${ENVOY_PORT:-80}

#####################################################################################################
# Create envoy.yaml
####################################################################################################

cat <<-EOF > /etc/envoy/envoy.yaml
static_resources:
  clusters:
    - name: service
      connect_timeout: 0.25s
      type: strict_dns
      dns_lookup_family: V4_ONLY
      load_assignment:
        cluster_name: backend_cluster
        endpoints:
          - lb_endpoints:
              - endpoint:
                  address:
                    socket_address:
                      address: $SERVICE_HOST
                      port_value: $SERVICE_PORT
    - name: keycloak_cluster
      connect_timeout: 0.25s
      type: strict_dns
      dns_lookup_family: V4_ONLY
      load_assignment:
        cluster_name: keycloak_cluster
        endpoints:
          - lb_endpoints:
              - endpoint:
                  address:
                    socket_address:
                      address: $KEYCLOAK_HOST
                      port_value: $KEYCLOAK_PORT
EOF

if [ $KEYCLOAK_PROTOCOL = "https" ]; then
cat <<-EOF >> /etc/envoy/envoy.yaml
      transport_socket:
        name: envoy.transport_sockets.tls
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
          sni: $KEYCLOAK_HOST
          common_tls_context:
            validation_context:
              trusted_ca:
                filename: /etc/ssl/ca.crt
EOF
fi

cat <<-EOF >> /etc/envoy/envoy.yaml
  listeners:
    - name: service_listener
      address:
        socket_address:
          address: 0.0.0.0
          port_value: $ENVOY_PORT
      filter_chains:
        - filters:
            - name: envoy.http_connection_manager
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                stat_prefix: ingress_http
                route_config:
                  name: local_route
                  virtual_hosts:
                    - name: backend
                      domains: ["*"]
                      routes:
                        - match:
                            prefix: "/"
                          route:
                            timeout: $TIMEOUT
                            cluster: service
                http_filters:
EOF

if [ ${DISABLE_JWT_AUTHN:-false} != "true" ]; then
cat <<-EOF >> /etc/envoy/envoy.yaml
                  - name: envoy.filters.http.jwt_authn
                    typed_config:
                      "@type": type.googleapis.com/envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication
                      providers:
                        keycloak:
                          issuer: $KEYCLOAK_URL/auth/realms/$KEYCLOAK_REALM
                          audiences:
                            - $SERVICE_NAME
                          remote_jwks:
                            http_uri:
                              uri: $KEYCLOAK_URL/auth/realms/$KEYCLOAK_REALM/protocol/openid-connect/certs
                              cluster: keycloak_cluster
                              timeout: 0.5s
                            cache_duration:
                              seconds: 300
                          forward: true
                      rules:
                        - match:
                            prefix: /
                          requires:
                            provider_name: keycloak
EOF
fi

cat <<-EOF >> /etc/envoy/envoy.yaml
                  - name: envoy.ext_authz
                    typed_config:
                      "@type": type.googleapis.com/envoy.extensions.filters.http.ext_authz.v3.ExtAuthz
                      failure_mode_allow: false
                      metadata_context_namespaces:
                        - envoy.filters.http.jwt_authn
                      grpc_service:
                        google_grpc:
                          target_uri: $OPA_HOST:$OPA_PORT
                          stat_prefix: ext_authz
                        timeout: 0.5s
                  - name: envoy.filters.http.router
                    typed_config: {}
EOF

cat /etc/envoy/envoy.yaml

/usr/local/bin/envoy -c /etc/envoy/envoy.yaml -l ${ENVOY_LOG_LEVEL:-info}

#!/bin/bash
sed -i 's/#AllowAgentForwarding yes/AllowAgentForwarding yes/g' /etc/ssh/sshd_config
sed -i 's/AllowTcpForwarding no/AllowTcpForwarding yes/g' /etc/ssh/sshd_config
sed -i 's/GatewayPorts no/GatewayPorts yes/g' /etc/ssh/sshd_config
sed -i 's/X11Forwarding no/X11Forwarding yes/g' /etc/ssh/sshd_config
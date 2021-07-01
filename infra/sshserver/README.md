# SSH Server

This is to create a SSH Server to proxy request from kubernetes to localhost for easy development. 

```bash
# Get user id
id <YOUR_CURRENT_USER>

# Change PUID / PGID according to your user id
vi docker-compose.yaml

# Start the docker service
docker-compose up -d
```

Try to Access the docker

```bash
# Shell Access
docker exec -it openssh-server /bin/bash

# Logs
docker logs -f openssh-server
```

SSH Commands

```bash
# SSH into container
ssh -p 2222 dev@localhost

# Remote Port Forwarding
ssh -R 8888:127.0.0.1:8888 -p 2222 dev@localhost
```

## References

- [OpenSSH Server Docker](https://github.com/linuxserver/docker-openssh-server)
- [OpenSSH Server SSH Tunnel Module](https://github.com/linuxserver/docker-mods/tree/openssh-server-ssh-tunnel)
- [OpenSSH Server SSH Tunnel Issue](https://github.com/linuxserver/docker-openssh-server/issues/22)

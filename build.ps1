# 1. Build the Docker image locally
docker build -t summerbeachhh/ctf-platform:latest .

# 2. Save the image to a tar file (for transfer to server)
docker save -o ctf-platform.tar summerbeachhh/ctf-platform:latest

Write-Host "Build complete. Please upload 'ctf-platform.tar' and 'docker-compose.yml' to your server."
Write-Host "On server run: docker load -i ctf-platform.tar && docker compose up -d"

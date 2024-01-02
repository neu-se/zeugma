# Extract the repository archive
FROM busybox AS extract
WORKDIR /extract
COPY zeugma-main.zip .
RUN unzip zeugma-main.zip

# Use Maven base image
FROM maven:3.9.6-eclipse-temurin-11-focal
WORKDIR /home
COPY --from=extract /extract/zeugma-main/. .
# Download python and pip
RUN apt-get update && \
    apt-get install -y python3.8 python3-pip
# Upgrade pip
RUN python3 -m pip install --upgrade --force-reinstall pip
# Install python requirements
RUN python3 -m pip install -r ./resources/requirements.txt
# Build the project
RUN mvn -DskipTests install
# Expose port 80
EXPOSE 80/tcp
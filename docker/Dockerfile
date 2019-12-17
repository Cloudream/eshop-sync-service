FROM java:8
MAINTAINER George <ubuntu_vip@sina.cn>
VOLUME /tmp
ADD eshop-sync-service.jar eshop-sync-service.jar
#RUN bash -c 'touch /eshop-sync-service.jar'
EXPOSE 8771
ENTRYPOINT ["java","-Xmx128m","-Djava.security.egd=file:/dev/./urandom","-jar","/eshop-sync-service.jar"]
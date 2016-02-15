# Java container
FROM java

MAINTAINER Peter Larsson, Callista Enterprise AB

LABEL app.name="Quiz App."

ENV TERM=xterm
ENV SERVER_PORT=80
ENV TZ=CET

RUN mkdir /tmp/home && ln -sf /usr/share/zoneinfo/Europe/Stockholm /etc/localtime

ADD target/quiz-cloud-0.3.1.jar /tmp/home/

EXPOSE 80

WORKDIR /tmp/home

ENTRYPOINT ["java"]

CMD ["-Xmx512m", "-Djava.security.egd=file:/dev/urandom", "-jar", "quiz-cloud-0.3.1.jar"]

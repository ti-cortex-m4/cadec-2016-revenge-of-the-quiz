# Python container
FROM python:2.7.11

MAINTAINER Peter Larsson, Callista Enterprise AB

LABEL app.name="Quiz App Admin."

RUN mkdir /tmp/home && ln -sf /usr/share/zoneinfo/Europe/Stockholm /etc/localtime

ADD qa /tmp/home/

ENV TERM=xterm
ENV TZ=CET

WORKDIR /tmp/home

ENTRYPOINT ["/tmp/home/qa"]


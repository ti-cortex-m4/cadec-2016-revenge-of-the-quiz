#!/bin/bash
curl -H "Content-Type: application/json; charset=utf-8" -XPOST -d@src/test/resources/quiz-test.json http://$(docker-machine ip default):8080/api/quizzes

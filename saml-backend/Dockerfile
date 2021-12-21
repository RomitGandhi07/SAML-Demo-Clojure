FROM openjdk:8-alpine

COPY target/uberjar/saml-backend.jar /saml-backend/app.jar

EXPOSE 7171

CMD ["java", "-jar", "/saml-backend/app.jar"]

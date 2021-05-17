name := "StopLoss-TakeProfit-bot"

version := "0.1"

scalaVersion := "2.12.8"

//openapi core
libraryDependencies ++= Seq(
  "io.swagger.core.v3" % "swagger-annotations" % "2.0.0",
  "javax.annotation" % "javax.annotation-api" % "1.3.2"
)

//openapi sdk-java8
libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.30",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.10.1",
  "com.fasterxml.jackson.core" % "jackson-annotations" % "2.10.1",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.1",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.10.1",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.10.1",
  "org.jetbrains" % "annotations" % "13.0",
  "org.reactivestreams" % "reactive-streams" % "1.0.3",
  "com.squareup.okhttp3" % "okhttp" % "4.9.0",
  "org.junit.jupiter" % "junit-jupiter" % "5.5.2",
  "org.mockito" % "mockito-core" % "3.2.4"
)

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "upickle" % "1.3.8",
  "com.lihaoyi" %% "os-lib" % "0.7.6",
  "org.typelevel" % "cats-effect_2.12" % "3.1.1",
  "org.scala-lang.modules" % "scala-java8-compat_2.12" % "1.0.0",
  "org.typelevel" % "cats-core_2.12" % "2.6.1",
  "com.bot4s" % "telegram-core_2.12" % "4.4.0-RC2",
  "com.softwaremill.sttp" % "core_2.12" % "1.7.2"
)

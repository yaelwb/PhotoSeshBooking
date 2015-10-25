name := """PhotoSesh"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "junit" % "junit" % "4.12" % "test",
  javaJpa,
  "org.json" % "json" % "20140107",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
//  "org.springframework" % "spring-context" % "4.2.1.RELEASE",
  "com.typesafe.play" % "play-java-jpa_2.11" % "2.4.1",
  "org.springframework" % "spring-test" % "4.2.1.RELEASE",
  "org.hibernate" % "hibernate-core" % "5.0.2.Final",
  "org.hibernate" % "hibernate-entitymanager" % "5.0.2.Final",
  "org.hibernate" % "hibernate-annotations" % "3.5.6-Final",
  "org.hibernate.java-persistence" % "jpa-api" % "2.0-cr-1",
  "org.hibernate.javax.persistence" % "hibernate-jpa-2.0-api" % "1.0.1.Final",
  "org.hibernate" % "hibernate-core" % "5.0.2.Final",
  "org.apache.commons" % "commons-csv" % "1.2",
  "com.google.inject" % "guice" % "4.0-beta"
)


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

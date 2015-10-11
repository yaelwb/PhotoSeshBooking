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
  "org.springframework" % "spring-context" % "4.2.1.RELEASE",
  "org.springframework.data" % "spring-data-jpa" % "1.8.2.RELEASE",
  "org.springframework" % "spring-test" % "4.2.1.RELEASE",
  "org.hibernate" % "hibernate-core" % "4.3.10.Final",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.10.Final",
  "org.apache.commons" % "commons-csv" % "1.2"
)


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

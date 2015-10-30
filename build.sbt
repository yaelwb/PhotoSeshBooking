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
  "org.springframework" % "spring-test" % "4.2.1.RELEASE",
  "org.hibernate" % "hibernate-core" % "5.0.2.Final",
  "org.hibernate" % "hibernate-entitymanager" % "5.0.2.Final",
  "org.hibernate" % "hibernate-annotations" % "3.5.6-Final",
  "org.apache.commons" % "commons-csv" % "1.2",
  "com.google.inject" % "guice" % "4.0-beta",
  "org.mockito" % "mockito-core" % "1.10.19",
  "org.mockito" % "mockito-all" % "1.10.19",
  "org.apache.derby" % "derby" % "10.12.1.1" % "test",
  "org.hibernate" % "hibernate-entitymanager" % "5.0.2.Final" % "test"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

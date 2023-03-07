import java.time.LocalDateTime
import java.time.Instant
import java.util.TimeZone

import sbt._
import Keys._
import sbt.Package.ManifestAttributes

val now = System.currentTimeMillis

name := "synechron-test"
version := "1.0.0"

scalaVersion := "2.13.8"

resolvers += "Maven Central Server" at "https://repo1.maven.org/maven2"
resolvers += "Typesafe Server" at "https://repo.typesafe.com/typesafe/releases"
resolvers += "Sonatype Server" at "https://oss.sonatype.org/content/repositories/releases/"


//JAXB were removed from Java 9 and up
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.3.1"

//Spring Boot
libraryDependencies += "org.springframework.boot" % "spring-boot-starter-logging" % "2.6.7" //some modules import slf4j-api 2.0.0-alpha1 which is not compatible with logback (also in SpringBoot)
libraryDependencies += "org.springframework.boot" % "spring-boot-starter-web" % "2.6.7" exclude("org.slf4j", "slf4j-api")
libraryDependencies += "org.springframework.boot" % "spring-boot-starter-test" % "2.6.7" % Test exclude("org.slf4j", "slf4j-api")
libraryDependencies += "org.springframework.boot" % "spring-boot-starter-security" % "2.6.7" exclude("org.slf4j", "slf4j-api")
libraryDependencies += "org.springframework" % "spring-tx" % "5.3.25"

//Apache Ignite
libraryDependencies += "org.apache.ignite" % "ignite-spring" % "2.11.1"
libraryDependencies += "org.apache.ignite" % "ignite-indexing" % "2.11.1" exclude("com.h2database", "h2")
libraryDependencies += "org.apache.ignite" % "ignite-slf4j" % "2.11.1"

//Libs
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6.1"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.12.0"
libraryDependencies += "org.apache.commons" % "commons-text" % "1.9"

// !!! TESTS !!!

//libraryDependencies += "org.junit.jupiter" % "junit-jupiter-engine" % "5.5.2" % Test
//libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test
libraryDependencies += "org.awaitility" % "awaitility-scala" % "4.1.1" % Test
libraryDependencies += "junit" % "junit" % "4.13.2" //% Test some projects (Apache Tika, OpenJMAI) import junit explicitly
libraryDependencies += "com.github.sbt" % "junit-interface" % "0.13.3" % Test exclude("junit", "junit-dep")

//Starting with Spring Boot 2.4, JUnit 5’s vintage engine has been removed from spring-boot-starter-test. If we still want to write tests using JUnit 4
//libraryDependencies += "org.junit.vintage" % "junit-vintage-engine" % "5.7.2" % Test exclude("org.hamcrest","hamcrest-core")

//runs test via 'sbt clean test'
//use 'brew upgrade sbt'
testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "--verbosity=3", "-a")  //shows jUnit tests log events at INFO level
Test / parallelExecution := false
Test / fork := true
crossPaths := false
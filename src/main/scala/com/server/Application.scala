package com.server

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}

object Application {
  def main(args: Array[String]) : Unit = SpringApplication.run(classOf[Application], args :_ *)
}

@SpringBootApplication
@EnableAutoConfiguration(
  exclude=Array(classOf[DataSourceAutoConfiguration], classOf[HibernateJpaAutoConfiguration]) //disable datasource auto-load
)
class Application {
}

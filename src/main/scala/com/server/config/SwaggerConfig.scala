package com.server.config

import org.apache.commons.logging.LogFactory
import org.springframework.boot.web.context.WebServerInitializedEvent
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.context.event.EventListener
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class SwaggerConfig extends WebMvcConfigurer {

  val logger = LogFactory.getLog(getClass)

  @EventListener(Array(classOf[WebServerInitializedEvent]))
  def onWebServerInitializedEvent(event : WebServerInitializedEvent) : Unit = {
    val port = event.getWebServer.getPort
    logger.info(s"Swagger API is available at http://localhost:${port}/swagger-ui/index.html")
  }
}
package com.server

import com.server.service.TranslateService
import org.apache.ignite.spi.discovery.DiscoverySpi
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.{Bean, Primary}

import java.util.Collections

@TestConfiguration
class ServerTestConfiguration {

  @Bean
  @Primary
  def translateService: TranslateService = Mockito.mock(classOf[TranslateService])


  @Bean
  @Primary
  //set null-discovery finder for tests
  def igniteSpiDiscovery : DiscoverySpi = {
    val igniteDiscoveryFinder = new TcpDiscoveryVmIpFinder
    igniteDiscoveryFinder.setShared(true);
    igniteDiscoveryFinder.setAddresses(Collections.singletonList("127.0.0.1"))

    val tcpDiscovery = new TcpDiscoverySpi
    tcpDiscovery.setIpFinder(igniteDiscoveryFinder)

    tcpDiscovery
  }

}
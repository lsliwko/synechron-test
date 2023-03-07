package com.server.config

import javax.annotation.{PostConstruct, PreDestroy}
import org.apache.commons.logging.LogFactory
import org.apache.ignite.cache.CacheAtomicityMode
import org.apache.ignite.cluster.ClusterState
import org.apache.ignite.{Ignite, IgniteSpring, Ignition, ShutdownPolicy}
import org.apache.ignite.configuration.{CacheConfiguration, IgniteConfiguration}
import org.apache.ignite.logger.slf4j.Slf4jLogger
import org.apache.ignite.spi.discovery.DiscoverySpi
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.{Bean, Configuration}

import scala.util.Try
import scala.util.control.NonFatal
import scala.jdk.CollectionConverters._
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder

@Configuration
class IgniteCacheConfig {

  val logger = LogFactory.getLog(getClass)

  @ConditionalOnMissingBean
  @Bean
  def igniteSpiDiscovery : DiscoverySpi = {
    //NOTE: Discovery can work in many modes, i.e. TCP multi-cast, shared S3 bucket, etc.

    val igniteDiscoveryFinder = new TcpDiscoveryMulticastIpFinder

    val tcpDiscovery = new TcpDiscoverySpi
    tcpDiscovery.setIpFinder(igniteDiscoveryFinder)

    tcpDiscovery
  }

  @ConditionalOnMissingBean
  @Bean
  def igniteClient(
    applicationContext : ApplicationContext,
    igniteSpiDiscovery : DiscoverySpi
  ): Ignite = {
    logger.info(s"Starting Ignite Cache...")

    val igniteConfiguration = new IgniteConfiguration

    igniteConfiguration.setGridLogger(new Slf4jLogger())

    igniteConfiguration.setDiscoverySpi(igniteSpiDiscovery)

    val igniteCacheConfiguration = new CacheConfiguration(IgniteCacheConfig.IGNITE_CACHE_NAME)
    igniteCacheConfiguration.setAtomicityMode(CacheAtomicityMode.ATOMIC)

    igniteConfiguration.setCacheConfiguration(igniteCacheConfiguration)

    //Node will stop if and only if it does not store any unique partitions, that don't have another copies in the cluster.
    //igniteConfiguration.setShutdownPolicy(ShutdownPolicy.GRACEFUL) data is available in database and will be loaded by node if needed

    val igniteGrids = Ignition.allGrids().asScala
    logger.debug(s"Running Ignite Cache grids: ${igniteGrids.map(_.name()).mkString(",")}")

    val igniteClient = {
      if (igniteGrids.isEmpty) {
        IgniteSpring.start(igniteConfiguration, applicationContext)
      } else if (igniteGrids.size==1) {
        logger.warn("Ignite Cache already started (this is normal in tests)")
        igniteGrids.head
      } else {
        //exception will stop Spring Boot
        throw new IllegalStateException("More than one Ignite Cache already started (should not happen)")
      }
    }

    igniteClient
  }

  @Bean
  def igniteCacheBean(
    igniteClient : Ignite
  ) = new {

    @PostConstruct
    def onCreate() : Unit = {
      logger.info("Setting up Ignite Cluster Termination Bean")

      //if cluster is not active, activate it
      if (igniteClient.cluster().state() != ClusterState.ACTIVE) {

        logger.info("Activating Ignite Cache cluster...")
        igniteClient.cluster().state(ClusterState.ACTIVE)
      }
    }

    @PreDestroy
    def onDestroy : Unit = {

      val nodesCount  = Try { igniteClient.cluster().nodes().size() }.getOrElse(0)
      logger.info(s"Terminating Ignite Cluster (nodes: ${nodesCount})...")

      //close Ignite cluster
      Try (
        if (nodesCount<=1) {
          logger.info("Terminating Ignite Cache (this the the last node)...")

          Try { igniteClient.cluster().shutdownPolicy(ShutdownPolicy.IMMEDIATE) }
          Ignition.stopAll(true)  //If wait parameter is set to true then grid will wait for all
        }
      ).recover { case NonFatal(exception) => logger.error("Error closing resource (ignored)", exception) }

    }

  }
}

object IgniteCacheConfig {
  val IGNITE_CACHE_NAME = "server-cache"
}
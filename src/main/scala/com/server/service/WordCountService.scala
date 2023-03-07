package com.server.service

import org.apache.commons.lang3.StringUtils
import org.apache.commons.logging.LogFactory
import org.apache.ignite.cache.{CacheEntry, CacheEntryProcessor}
import org.apache.ignite.{Ignite, IgniteCache}
import org.apache.ignite.cache.query.ScanQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.{ContextRefreshedEvent, EventListener}
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service

import javax.cache.processor.MutableEntry
import scala.jdk.CollectionConverters._

@Service
class WordCountService {

  val logger = LogFactory.getLog(getClass)

  @Autowired
  val igniteClient : Ignite = null

  @Autowired
  val translateService : TranslateService = null

  private var wordCountCache : IgniteCache[String, Long] = null

  def increaseCount(word : String) : Either[String,Long] =
    validateAndTranslateWord(word).map { word =>
      //atomic increment
      wordCountCache.invoke(word, new CacheEntryProcessor[String,Long,Long]() {
        override def process(entry: MutableEntry[String, Long], arguments: Object*): Long = {
          val newValue = entry.getValue + 1
          entry.setValue(newValue)
          newValue
        }
      })
    }

  def getCount(word : String) : Either[String,Long] =
    validateAndTranslateWord(word).map { word =>
      wordCountCache.get(word)
    }

  def resetCount(word : String) : Either[String,Long] =
    validateAndTranslateWord(word).map { word =>
      val oldValue = wordCountCache.get(word)
      wordCountCache.remove(word)
      oldValue
    }

  def getWordCountMap() : Map[String,Long] = {
    wordCountCache.query(new ScanQuery[String,Long]).getAll.asScala.map { entry => (entry.getKey, entry.getValue) }.toMap
  }

  private def validateAndTranslateWord(word : String) : Either[String,String] = {
    word match {
      case word if StringUtils.isBlank(word) => Left(s"Word is empty")
      case word if StringUtils.isAlphanumeric(word) => Right(word).map { word =>
        //NOTE: TranslateService should also normalise words, i.e. lowercase, remove whitespace, convert special characters, etc.
        translateService.translate(word)
      }
      case _ => Left(s"Word [$word] is not alphanumeric")
    }
  }

  @EventListener(Array(classOf[ContextRefreshedEvent]))
  @Order(/*START*/ 40)
  def onContextRefreshedEvent() : Unit = {
    logger.info("Creating word-count cache...")
    wordCountCache  = igniteClient.getOrCreateCache[String,Long]("word-count-cache")
  }


}
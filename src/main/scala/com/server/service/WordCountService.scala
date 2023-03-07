package com.server.service

import com.server.config.IgniteCacheConfig
import org.apache.commons.lang3.StringUtils
import org.apache.commons.logging.LogFactory
import org.apache.ignite.cache.CacheEntryProcessor
import org.apache.ignite.{Ignite, IgniteCache}
import org.apache.ignite.cache.query.ScanQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.{ContextRefreshedEvent, EventListener}
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service

import javax.cache.processor.{EntryProcessor, MutableEntry}

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

  private def validateAndTranslateWord(word : String) : Either[String,String] = {
    word match {
      case word if StringUtils.isBlank(word) => Left(s"Word is empty")
      case word if StringUtils.isAlphanumeric(word) => Right(word).map { word => translateService.translate(word) }
      case _ => Left(s"Word [$word] is not alphanumeric")
    }
  }

  @EventListener(Array(classOf[ContextRefreshedEvent]))
  @Order(/*START*/ 40)  //START early (after db is patched)
  def onContextRefreshedEvent() : Unit = {
    logger.info("Creating word-count cache...")

    wordCountCache  = igniteClient.getOrCreateCache[String,Long]("word-count-cache")
  }


}
package com.server.service

import org.apache.commons.lang3.StringUtils
import org.apache.commons.logging.LogFactory
import org.apache.ignite.Ignite
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class WordCountService {

  val logger = LogFactory.getLog(getClass)

  @Autowired
  val igniteClient : Ignite = null

  @Autowired
  val translateService : TranslateService = null

  def increaseCount(word : String) : Either[String,Long] =
    validateAndTranslateWord(word).map { word => igniteAtomicLong(word).incrementAndGet }

  def getCount(word : String) : Either[String,Long] =
    validateAndTranslateWord(word).map { word => igniteAtomicLong(word).get }

  def resetCount(word : String) : Either[String,Long] =
    validateAndTranslateWord(word).map { word => igniteAtomicLong(word).getAndSet(0) }

  private def validateAndTranslateWord(word : String) : Either[String,String] = {
    word match {
      case "" => Left(s"Word is empty")
      case word@_ if StringUtils.isAlphanumeric(word) => Right(word).map { word => translateService.translate(word) }
      case _ => Left(s"Word [$word] is not alphanumeric")
    }
  }

  private def igniteAtomicLong(word : String) = {
    igniteClient.atomicLong(
      s"word-count-$word",   //name of counter
      0,             //initial value
      true           //create if does not exist
    )
  }
}
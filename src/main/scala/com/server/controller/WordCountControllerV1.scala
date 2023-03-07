package com.server.controller

import com.server.service._

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._

@RestController
class WordCountControllerV1 {

  @Autowired
  val wordCountService : WordCountService = null

  @RequestMapping(
    value = Array("/api/v1/word/{word}/count"),
    method = Array(RequestMethod.GET)
  )
  def wordCount(
    @PathVariable("word") word : String
  ) =
    wordCountService.increaseCount(word) match {
      case Right(count) => ResponseEntity.status(HttpStatus.OK).body(count.toString)
      case Left(error) => ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

  @RequestMapping(
    value = Array("/api/v1/counts"),
    method = Array(RequestMethod.GET)
  )
  def wordCountMap() =
    ResponseEntity.status(HttpStatus.OK).body {
      //build body:
      //xxx=11
      //yyy=2
      //zzz=1
      wordCountService.getWordCountMap().toSeq.sortBy(-_._2).map { entry =>
        s"${entry._1}=${entry._2}"
      }.mkString("\n")
    }

}
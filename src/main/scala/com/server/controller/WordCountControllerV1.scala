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
    value = Array("/api/v1/{word}/count"),
    method = Array(RequestMethod.GET)
  )
  def wordCount(
    @PathVariable("word") word : String
  ) =
    wordCountService.increaseCount(word) match {
      case Right(count) => ResponseEntity.status(HttpStatus.OK).body(count.toString)
      case Left(error) => ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }


}
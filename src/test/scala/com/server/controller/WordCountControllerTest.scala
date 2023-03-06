package com.server.controller

import org.assertj.core.api.Assertions._
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.junit.{FixMethodOrder, Ignore, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.test.context.junit4.SpringRunner

import java.net.URI

@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class WordCountControllerTest {

  @LocalServerPort
  val port: Int = -1

  @Autowired
  val restTemplate : TestRestTemplate = null

  @Test
  @throws[Exception]
  def testWordIncrement {
    val response1 = apiV1WordCountGet("TestWordA")
    assertThat(response1.getStatusCode.value()).isEqualTo(HttpStatus.OK.value)
    val count1 = response1.getBody.toLong

    val response2 = apiV1WordCountGet("TestWordA")
    assertThat(response2.getStatusCode.value()).isEqualTo(HttpStatus.OK.value)
    val count2 = response2.getBody.toLong

    assertThat(count1 + 1).isEqualTo(count2)
  }

  @Test
  @throws[Exception]
  def testWordNonAlphanumeric {
    val response = apiV1WordCountGet("TestWordA!")
    assertThat(response.getStatusCode.value()).isEqualTo(HttpStatus.BAD_REQUEST.value)
    assertThat(response.getBody).isEqualTo("Word [TestWordA!] is not alphanumeric")
  }

  private def apiV1WordCountGet(word : String) : ResponseEntity[String] =
    restTemplate.getForEntity(
      apiV1WordCountUrl(word),
      classOf[String])

  private def apiV1WordCountUrl(word : String) = new URI(s"http://localhost:$port/api/v1/$word/count")

}

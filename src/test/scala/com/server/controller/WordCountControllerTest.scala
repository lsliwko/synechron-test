package com.server.controller

import com.server.ServerTestConfiguration
import com.server.service.{TranslateService, WordCountService}
import org.assertj.core.api.Assertions._
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.junit.{FixMethodOrder, Ignore, Test}
import org.mockito.Mockito.when
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner

import java.net.URI

@RunWith(classOf[SpringRunner])
@SpringBootTest(
  webEnvironment = WebEnvironment.RANDOM_PORT,
  properties = Array(
    "spring.main.allow-bean-definition-overriding=true"
  )
)
@Import(Array(classOf[ServerTestConfiguration]))
@DirtiesContext
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class WordCountControllerTest {

  @LocalServerPort
  val port: Int = -1

  @Autowired
  val restTemplate : TestRestTemplate = null

  @Autowired
  val translateService : TranslateService = null

  @Autowired
  val wordCountService : WordCountService = null

  @Test
  @throws[Exception]
  def testWordIncrement {
    when(translateService.translate("TestWordA")).thenReturn("TestWordA")

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

  @Test
  @throws[Exception]
  def testWordCountMap {
    when(translateService.translate("TestWordA")).thenReturn("TestWordA")
    when(translateService.translate("TestWordB")).thenReturn("TestWordB")
    when(translateService.translate("TestWordC")).thenReturn("TestWordC")

    wordCountService.resetCount("TestWordA")
    wordCountService.resetCount("TestWordB")
    wordCountService.resetCount("TestWordC")

    wordCountService.increaseCount("TestWordA")
    wordCountService.increaseCount("TestWordC")
    wordCountService.increaseCount("TestWordA")
    wordCountService.increaseCount("TestWordB")
    wordCountService.increaseCount("TestWordC")
    wordCountService.increaseCount("TestWordC")

    val responseMap = restTemplate.getForEntity(
      apiV1WordCountMapUrl,
      classOf[String])
    assertThat(responseMap.getStatusCode.value()).isEqualTo(HttpStatus.OK.value)

    assertThat(responseMap.getBody).isEqualTo(
      s"TestWordC=3\n" +
      s"TestWordA=2\n" +
      s"TestWordB=1")
  }

  private def apiV1WordCountGet(word : String) : ResponseEntity[String] =
    restTemplate.getForEntity(
      apiV1WordCountUrl(word),
      classOf[String])

  private def apiV1WordCountUrl(word : String) = new URI(s"http://localhost:$port/api/v1/word/$word/count")

  private def apiV1WordCountMapUrl() = new URI(s"http://localhost:$port/api/v1/counts")

}

package com.server.service

import com.server.ServerTestConfiguration
import org.assertj.core.api.Assertions._
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.junit.{FixMethodOrder, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.junit4.SpringRunner
import org.mockito.Mockito.when
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext

import scala.jdk.CollectionConverters._

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
class WordCountServiceTest {

  @Autowired
  val wordCountService : WordCountService = null

  @Autowired
  val translateService : TranslateService = null


  //NOTE: ScalaTest does not work nicely with SpringRunner

  @Test
  def testWordIncrement {

    when(translateService.translate("TestWordA")).thenReturn("TestWordA")
    when(translateService.translate("TestWordB")).thenReturn("TestWordB")
    when(translateService.translate("TestWordC")).thenReturn("TestWordC")

    wordCountService.resetCount("TestWordA")
    wordCountService.resetCount("TestWordB")
    wordCountService.resetCount("TestWordC")

    assertThat(wordCountService.getCount("TestWordA")).isEqualTo(Right(0))
    assertThat(wordCountService.getCount("TestWordB")).isEqualTo(Right(0))
    assertThat(wordCountService.getCount("TestWordC")).isEqualTo(Right(0))

    assertThat(wordCountService.increaseCount("TestWordA")).isEqualTo(Right(1))
    assertThat(wordCountService.increaseCount("TestWordB")).isEqualTo(Right(1))
    assertThat(wordCountService.increaseCount("TestWordA")).isEqualTo(Right(2))
    assertThat(wordCountService.increaseCount("TestWordA")).isEqualTo(Right(3))
    assertThat(wordCountService.increaseCount("TestWordB")).isEqualTo(Right(2))

    assertThat(wordCountService.getCount("TestWordA")).isEqualTo(Right(3))
    assertThat(wordCountService.getCount("TestWordB")).isEqualTo(Right(2))
    assertThat(wordCountService.getCount("TestWordC")).isEqualTo(Right(0))
  }

  @Test
  def testWordIncrementTranslation {

    when(translateService.translate("TestWordA")).thenReturn("TestWordA")
    when(translateService.translate("TestWordAVariant")).thenReturn("TestWordA")

    wordCountService.resetCount("TestWordA")

    assertThat(wordCountService.increaseCount("TestWordA")).isEqualTo(Right(1))
    assertThat(wordCountService.increaseCount("TestWordAVariant")).isEqualTo(Right(2))

    assertThat(wordCountService.getCount("TestWordA")).isEqualTo(Right(2))
    assertThat(wordCountService.getCount("TestWordAVariant")).isEqualTo(Right(2))
  }

  @Test
  def testWordCountMap {

    when(translateService.translate("TestWordA")).thenReturn("TestWordA")
    when(translateService.translate("TestWordB")).thenReturn("TestWordB")
    when(translateService.translate("TestWordC")).thenReturn("TestWordC")

    wordCountService.resetCount("TestWordA")
    wordCountService.resetCount("TestWordB")
    wordCountService.resetCount("TestWordC")

    assertThat(wordCountService.increaseCount("TestWordA")).isEqualTo(Right(1))
    assertThat(wordCountService.increaseCount("TestWordB")).isEqualTo(Right(1))
    assertThat(wordCountService.increaseCount("TestWordA")).isEqualTo(Right(2))

    assertThat(wordCountService.getWordCountMap().asJava).containsAllEntriesOf(
      Map(
        ("TestWordA" -> 2l),
        ("TestWordB" -> 1l)
      ).asJava
    )
  }


  @Test
  def testWordNonAlphanumeric {
    assertThat(wordCountService.getCount("TestWord!")).isEqualTo(Left("Word [TestWord!] is not alphanumeric"))
  }

  @Test
  def testWordEmpty {
    assertThat(wordCountService.getCount("")).isEqualTo(Left("Word is empty"))
  }

}
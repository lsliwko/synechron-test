# NOTES

1. Application uses Spring Boot as it provides very useful controller annotations.

2. Apache Ignite is distributed memory cache with atomic operations. It supports persistence via a number of store-like solutions (thus data is not lost after all nodes are down).

3. Apache Ignite uses TCP multi-cast to find other nodes in the network. If deployed in Kubernetes it can use Kubernetes finder or S3-based finder if used on AWS (this can be changed in IgniteCacheConfig.scala)

4. ScalaTest does not work nicely with SpringRunner (which is used to test controller / service), thus the tests were created with JUnit.

5. The specification mentioned sorting / retrieving all word counts. For it to work, applications executes QueryScan over distributed cache. It's implemented as WordCountService.getWordCountMap.

6. To open Swagger for api visit http://localhost:8080/swagger-ui/index.html#/word-count-controller-v-1/wordCount

![Usage GUI 1](screencapture-localhost-8080-swagger-ui.png "Usage GUI 1")

package ch.keepcalm.demo.service.customer

import io.jaegertracing.internal.samplers.ConstSampler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.info.GitProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.stream.Collectors
import javax.annotation.PostConstruct

@EnableDiscoveryClient
@SpringBootApplication
class CustomerServiceApplication

fun main(args: Array<String>) {
    runApplication<CustomerServiceApplication>(*args)
}

@Component
class TracerConfiguration {

    @Bean
    fun jaegerTracer(): io.jaegertracing.Configuration = io.jaegertracing.Configuration("customer-service")
            .withSampler(io.jaegertracing.Configuration.SamplerConfiguration
                    .fromEnv()
                    .withType(ConstSampler.TYPE)
                    .withParam(1))
            .withReporter(io.jaegertracing.Configuration.ReporterConfiguration
                    .fromEnv()
                    .withLogSpans(true))
}

@RestController
@RequestMapping("/api/v1/scientists")
class ScientistsNameResource(private val scientistsNameService: ScientistsNameService) {


    @GetMapping(path = ["/random"])
    fun getRandomName(@RequestHeader headers: HttpHeaders): String {
        return scientistsNameService.getRandomNames()
    }
}

@Service
class ScientistsNameService(var scientists: List<String> = listOf()) {

    @PostConstruct
    private fun init() {
        val inputStream = ClassPathResource("/scientists.txt").inputStream
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            scientists = reader.lines().collect(Collectors.toList<String>())
        }
    }

    fun getRandomNames() = scientists[kotlin.random.Random.nextInt(scientists.size)]

}

@Configuration
@EnableSwagger2
class SwaggerConfig(var build: Optional<BuildProperties>, var git: Optional<GitProperties>) {

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .apiInfo(ApiInfoBuilder()
                        .title("Spring Boot REST API")
                        .description("Customer Service REST API")
                        .contact(Contact("Marcel Widmer", "https://github.com/marzelwidmer", "marzelwidmer@gmail.com"))
                        .license("Apache 2.0")
                        .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                        .version(
                                when {
                                    (build.isPresent && git.isPresent) -> "${build.get().version}-${git.get().shortCommitId}-${git.get().branch}"
                                    else -> "1.0"
                                }
                        )
                        .build())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths { it.equals("/api/v1/scientists/random") }
                .build()
                .useDefaultResponseMessages(false)
                .forCodeGeneration(true)
    }
}
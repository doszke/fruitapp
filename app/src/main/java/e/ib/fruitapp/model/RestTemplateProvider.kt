package e.ib.fruitapp.model

import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate

object RestTemplateProvider {

    private val restTemplate = RestTemplate()

    init {
        restTemplate.messageConverters.add(StringHttpMessageConverter())
    }

    fun provide() : RestTemplate {
        return restTemplate
    }


}
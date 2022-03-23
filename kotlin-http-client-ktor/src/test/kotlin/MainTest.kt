import com.linkedplanet.kotlinhttpclient.AbstractMainTest
import com.linkedplanet.kotlinhttpclient.api.http.InsightConfig
import com.linkedplanet.kotlinhttpclient.core.InsightSchemaCacheOperator
import com.linkedplanet.kotlinhttpclient.ktor.KtorHttpClient
import org.junit.BeforeClass


class MainTest: AbstractMainTest() {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            println("#### Starting setUp")
            val httpClient = KtorHttpClient(
                "http://localhost:8080",
                "admin",
                "admin"
            )
            InsightConfig.init("http://localhost:8080", httpClient, InsightSchemaCacheOperator)
        }
    }
}
package it.com.linkedplanet.plugin.confluence.insightwrapper.test

import com.atlassian.applinks.api.ApplicationLinkService
import com.atlassian.applinks.api.application.jira.JiraApplicationType
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import com.atlassian.confluence.user.UserAccessor
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner
import com.atlassian.sal.api.ApplicationProperties
import com.linkedplanet.kotlininsightwrapper.AbstractMainTest
import com.linkedplanet.kotlininsightwrapper.api.http.InsightConfig
import com.linkedplanet.kotlininsightwrapper.atlas.AtlasHttpClient
import com.linkedplanet.kotlininsightwrapper.core.InsightSchemaCacheOperator
import org.junit.Before
import org.junit.runner.RunWith

@RunWith(AtlassianPluginsTestRunner::class)
class MainWiredTest : AbstractMainTest {

    private lateinit var userAccessor: UserAccessor
    private lateinit var applicationProperties: ApplicationProperties
    private lateinit var applicationLinkService: ApplicationLinkService

    constructor(
        userAccessor: UserAccessor,
        applicationProperties: ApplicationProperties,
        applicationLinkService: ApplicationLinkService
    ) {
        this.userAccessor = userAccessor
        this.applicationProperties = applicationProperties
        this.applicationLinkService = applicationLinkService
        println("### Starting MainWiredTest")
        println("### AppLinkUrl: ${applicationLinkService.getPrimaryApplicationLink(JiraApplicationType::class.java).displayUrl}")
        val serviceUser = userAccessor.getUserByName("admin")
        AuthenticatedUserThreadLocal.asUser(serviceUser)
        val appLink = applicationLinkService.getPrimaryApplicationLink(JiraApplicationType::class.java)
        val httpClient = AtlasHttpClient(
            appLink
        )
        InsightConfig.init("http://localhost:8080", httpClient, InsightSchemaCacheOperator)
        println("### Starting MainWiredTest")
    }

    @Before
    fun initTest() {
        val serviceUser = userAccessor.getUserByName("admin")
        AuthenticatedUserThreadLocal.asUser(serviceUser)
    }

}
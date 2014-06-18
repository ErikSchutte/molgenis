package org.molgenis.elasticsearch.config;

import java.io.File;
import java.util.Collections;

import org.molgenis.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.search.SearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring config for embedded elastic search server. Use this in your own app by importing this in your spring config:
 * <code> @Import(EmbeddedElasticSearchConfig.class)</code>
 * 
 * @author erwin
 * 
 */
@Configuration
public class EmbeddedElasticSearchConfig
{
	private static final String MOLGENIS_INDEX_NAME = "molgenis";

	@Bean(destroyMethod = "close")
	public EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory()
	{
		// get molgenis home directory
		String molgenisHomeDir = System.getProperty("molgenis.home");
		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException("missing required java system property 'molgenis.home'");
		}
		if (!molgenisHomeDir.endsWith("/")) molgenisHomeDir = molgenisHomeDir + '/';

		// create molgenis data directory if not exists
		String molgenisDataDirStr = molgenisHomeDir + "data";
		File molgenisDataDir = new File(molgenisDataDirStr);
		if (!molgenisDataDir.exists())
		{
			if (!molgenisDataDir.mkdir())
			{
				throw new RuntimeException("failed to create directory: " + molgenisDataDirStr);
			}
		}

		return new EmbeddedElasticSearchServiceFactory(Collections.singletonMap("path.data", molgenisDataDirStr));
	}

	@Bean
	public ElasticSearchClient client()
	{
		return new ElasticSearchClient(embeddedElasticSearchServiceFactory().getClient(), MOLGENIS_INDEX_NAME);
	}

	@Bean
	public SearchService searchService()
	{
		return embeddedElasticSearchServiceFactory().create();
	}
}

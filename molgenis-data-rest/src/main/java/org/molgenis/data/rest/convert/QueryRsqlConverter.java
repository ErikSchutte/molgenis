package org.molgenis.data.rest.convert;

import org.molgenis.data.rsql.QueryRsql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;

public class QueryRsqlConverter implements Converter<String, QueryRsql>
{
	private final RSQLParser rsqlParser;

	@Autowired
	public QueryRsqlConverter(RSQLParser rsqlParser)
	{
		this.rsqlParser = rsqlParser;
	}

	@Override
	public QueryRsql convert(String source)
	{
		Node rootNode = rsqlParser.parse(source);
		return new QueryRsql(rootNode);
	}
}
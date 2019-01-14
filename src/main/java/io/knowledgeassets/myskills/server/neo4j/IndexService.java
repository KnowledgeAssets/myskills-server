package io.knowledgeassets.myskills.server.neo4j;

import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.request.DefaultRequest;
import org.neo4j.ogm.session.request.RowDataStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.knowledgeassets.myskills.server.neo4j.IndexType.*;
import static java.util.Collections.emptyMap;
import static java.util.regex.Pattern.compile;
import static org.neo4j.ogm.transaction.Transaction.Type.READ_WRITE;

@Service
public class IndexService {

	private final Neo4jSession session;

	@Autowired
	public IndexService(SessionFactory sessionFactory) {
		this.session = (Neo4jSession) sessionFactory.openSession();
	}

	public List<String> loadIndexesFromDB() {
		DefaultRequest indexRequests = buildProcedures();
		List<String> dbIndexCreateStatements = new ArrayList<>();
		session.doInTransaction(() -> {
			try (Response<RowModel> response = session.requestHandler().execute(indexRequests)) {
				RowModel rowModel;
				while ((rowModel = response.next()) != null) {
					String index = parseIndexToCreateStatement((String) rowModel.getValues()[0]);
					if (index != null) {
						dbIndexCreateStatements.add(index);
					}
				}
			}
		}, READ_WRITE);

		return dbIndexCreateStatements;
	}

	public void executeStatements(List<String> strStatements) {
		List<Statement> createStatements = new ArrayList<>();
		strStatements.forEach(s -> createStatements.add(new RowDataStatement(s, emptyMap())));

		DefaultRequest request = new DefaultRequest();
		request.setStatements(createStatements);

		session.doInTransaction(() -> {
			try (Response<RowModel> response = session.requestHandler().execute(request)) {
				// Success
			}
		}, READ_WRITE);
	}

	private DefaultRequest buildProcedures() {
		List<Statement> procedures = new ArrayList<>();

		procedures.add(new RowDataStatement("CALL db.constraints()", emptyMap()));
		procedures.add(new RowDataStatement("call db.indexes() yield description, type with description, type where type <> 'node_unique_property' return description", emptyMap()));

		DefaultRequest getIndexesRequest = new DefaultRequest();
		getIndexesRequest.setStatements(procedures);
		return getIndexesRequest;
	}

	String parseIndexToCreateStatement(String indexValue) {
		Pattern pattern;
		Matcher matcher;

		pattern = compile("INDEX ON :(?<label>.*)\\((?<property>.*)\\)");
		matcher = pattern.matcher(indexValue);
		if (matcher.matches()) {
			String label = matcher.group("label");
			String[] properties = matcher.group("property").split(",");
			for (int i = 0; i < properties.length; i++) {
				properties[i] = properties[i].trim();
			}

			if (properties.length > 1) {
				return COMPOSITE_INDEX.convertToCreateCommand(label, properties);
			} else {
				return SINGLE_INDEX.convertToCreateCommand(label, properties);
			}
		}

		pattern = compile("CONSTRAINT ON \\((?<name>.*):(?<label>.*)\\) ASSERT ?\\k<name>.(?<property>.*) IS UNIQUE");
		matcher = pattern.matcher(indexValue);
		if (matcher.matches()) {
			String label = matcher.group("label").trim();
			String[] properties = matcher.group("property").split(",");
			return UNIQUE_CONSTRAINT.convertToCreateCommand(label, properties);
		}

		pattern = compile("CONSTRAINT ON \\((?<name>.*):(?<label>.*)\\) ASSERT \\((?<properties>.*)\\) IS NODE KEY");
		matcher = pattern.matcher(indexValue);
		if (matcher.matches()) {
			String label = matcher.group("label").trim();
			String[] properties = matcher.group("properties").split(",");
			for (int i = 0; i < properties.length; i++) {
				properties[i] = properties[i].trim().substring(label.length() + 1);
			}
			return NODE_KEY_CONSTRAINT.convertToCreateCommand(label, properties);
		}

		pattern = compile(
				"CONSTRAINT ON \\(\\s?(?<name>.*):(?<label>.*)\\s?\\) ASSERT exists\\(?\\k<name>.(?<property>.*)\\)");
		matcher = pattern.matcher(indexValue);
		if (matcher.matches()) {
			String label = matcher.group("label").trim();
			String[] properties = matcher.group("property").split(",");
			return NODE_PROP_EXISTENCE_CONSTRAINT.convertToCreateCommand(label, properties);
		}

		pattern = compile(
				"CONSTRAINT ON \\(\\)-\\[\\s?(?<name>.*):(?<label>.*)\\s?]-\\(\\) ASSERT exists\\(?\\k<name>.(?<property>.*)\\)");
		matcher = pattern.matcher(indexValue);
		if (matcher.matches()) {
			String label = matcher.group("label").trim();
			String[] properties = matcher.group("property").split(",");
			for (int i = 0; i < properties.length; i++) {
				properties[i] = properties[i].trim();
			}
			return REL_PROP_EXISTENCE_CONSTRAINT.convertToCreateCommand(label, properties);
		}

		return null;
	}
}

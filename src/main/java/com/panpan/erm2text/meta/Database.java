package com.panpan.erm2text.meta;

import java.util.ArrayList;
import java.util.List;

public class Database {
	
	private List<Table> tables = new ArrayList<Table>();
	private List<Relationship> relationships = new ArrayList<Relationship>();
	private List<Domain> domains = new ArrayList<Domain>();
	private List<String> sequences = new ArrayList<String>();
	
	public List<Table> getTables() {
		return tables;
	}
	public void setTables(List<Table> tables) {
		this.tables = tables;
	}
	public List<Relationship> getRelationships() {
		return relationships;
	}
	public void setRelationships(List<Relationship> relationships) {
		this.relationships = relationships;
	}
	public List<Domain> getDomains()
	{
		return domains;
	}
	public void setDomains(List<Domain> domains)
	{
		this.domains = domains;
	}
	public List<String> getSequences() {
		return sequences;
	}
	public void setSequences(List<String> sequences) {
		this.sequences = sequences;
	}
}

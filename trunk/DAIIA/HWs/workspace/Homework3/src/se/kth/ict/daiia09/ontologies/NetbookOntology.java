package se.kth.ict.daiia09.ontologies;

import jade.content.onto.BasicOntology;
import jade.content.onto.Introspector;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;

public class NetbookOntology extends Ontology implements NetbookVocabulary {
	public static final String ONTOLOGY_NAME = "NetbookOntology";

    private static Ontology theInstance = new NetbookOntology(BasicOntology.getInstance());

    public static Ontology getInstance() {
        return theInstance;
    }
	private NetbookOntology(Ontology introspector) {
		super(ONTOLOGY_NAME, introspector);
		
		try {
			add(new ConceptSchema(NETBOOK), Netbook.class);
			add(new ConceptSchema(DELL_NETBOOK), Dell.class);
			add(new ConceptSchema(ASUS_NETBOOK), Asus.class);
			
			add(new PredicateSchema(COSTS), Costs.class);
			add(new PredicateSchema(OWNS), Owns.class);
			
			ConceptSchema cs = (ConceptSchema) getSchema(NETBOOK);
			cs.add(PRICE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(SCREEN_SIZE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(WEIGHT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			
			cs = (ConceptSchema) getSchema(DELL_NETBOOK);
			cs.addSuperSchema((ConceptSchema) getSchema(NETBOOK));
			
			cs = (ConceptSchema) getSchema(ASUS_NETBOOK);
			cs.addSuperSchema((ConceptSchema) getSchema(NETBOOK));
			
			PredicateSchema ps = (PredicateSchema) getSchema(OWNS);
			ps.add(OWNS_OWNER, (ConceptSchema) getSchema(BasicOntology.AID));
			ps.add(OWNS_NETBOOK, (ConceptSchema) getSchema(NETBOOK));
			
			ps = (PredicateSchema) getSchema(COSTS);
			ps.add(COSTS_ITEM, (ConceptSchema) getSchema(NETBOOK));
			ps.add(COSTS_PRICE, (ConceptSchema) getSchema(BasicOntology.INTEGER));
			
			
		} catch (OntologyException e) {
			e.printStackTrace();
		}
	}

}

/*
 * This class converts the relation extracted using RelEx to OWL
 * by Rui P. Costa (adapted from SimpleView and RelationView)
 *
 * Created in 2008 by "Rui Costa" <racosta@student.dei.uc.pt>,
 *                    "Rui Costa" * <racosta@student.dei.uc.pt>
 */
package relex.output;

import java.net.URI;
import java.util.HashMap;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLLabelAnnotation;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.util.SimpleURIMapper;

import relex.feature.FeatureNode;
import relex.feature.RelationCallback;
import relex.ParsedSentence;

/**
 * Implements a very simple, direct printout of the
 * RelEx feature graph in OWL.
 *
 * Copyright (c) 2008 Linas Vepstas <linas@linas.org> and Rui P. Costa <b4h0pe@gmail.com>
 */
public class OWLView
{

	private String sent;
	private OWLClass sentence;
	private OWLClass word;
	private OWLClass relex_word;
	private HashMap<String,OWLIndividual> map_owl_relexwords;
	private HashMap<String,OWLProperty> map_owl_properties;
	private HashMap<String,OWLIndividual> phr_type_map_owl;

	private OWLOntologyManager manager;
	private URI ontologyURI;
	private URI physicalURI;
	private OWLOntology ontology;
	private OWLDataFactory factory;
	private OWLIndividual sentenceInd;
	private int sentence_id;
	private OWLObjectProperty has;
	boolean viz = false;
	boolean viz_sentence = false;

	public OWLView() {
		initOntology(); //Change to the constructor in order to happen just one time
	}

	public OWLView(boolean viz, boolean viz_sentence) {
		initOntology(); //Change to the constructor in order to happen just one time

		this.viz = viz;
		this.viz_sentence = viz_sentence;
	}



	/**
	* Print out RelEx relations. All relations shown
	* in a binary form.
	*
	* Example:
	*	_subj(throw, John)
	*	_obj(throw, ball)
	*	tense(throw, past)
	*	definite-FLAG(ball, T)
	*	noun_number(ball, singular)
	*/
	public void printRelations(ParsedSentence parse, String sentence, int sentence_id, String ontologyname)
	{
		try
		{
			sent = sentence;

			//Add the sentence to Sentence Class
			this.sentence_id = sentence_id;
			sentenceInd = factory.getOWLIndividual(URI.create(ontologyURI + "#" + "sentence_" + sentence_id));

			OWLLabelAnnotation label = factory.getOWLLabelAnnotation(sent);

			OWLClassAssertionAxiom sentClass = factory.getOWLClassAssertionAxiom(sentenceInd, this.sentence);
			OWLAxiomAnnotationAxiom labelClass = factory.getOWLAxiomAnnotationAxiom(sentClass, label);
			manager.applyChange(new AddAxiom(ontology, sentClass));
			manager.applyChange(new AddAxiom(ontology, labelClass));

			printRelations(parse, null);

		}
		catch (OWLOntologyChangeException ex)
		{
			Logger.getLogger(OWLView.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void initOntology()
	{
		try
		{
			//Auxiliar structures
			//map_owl_relexwords = new HashMap<String,OWLIndividual>();
			map_owl_properties = new HashMap<String,OWLProperty>();

			// OWLOntologyManager that manages a set of ontologies
			manager = OWLManager.createOWLOntologyManager();

			// URI of the ontology
			ontologyURI = URI.create("http://student.dei.uc.pt/~racosta/relex/owl_format.owl");

			// Pphysical URI
			physicalURI = URI.create("file:/media/Docs/uc/MSc-2/SW/Project/ontologies/relex2.owl");

			// Set up a mapping, which maps the ontology URI to the physical URI
			SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
			manager.addURIMapper(mapper);

			// Now create the ontology - we use the ontology URI
			ontology = manager.createOntology(physicalURI);
			//Data factory, allows to manipulate ontology data
			factory = manager.getOWLDataFactory();

			sentence = factory.getOWLClass(URI.create(ontologyURI + "#Sentence"));
			word = factory.getOWLClass(URI.create(ontologyURI + "#Word"));
			//relex_word = factory.getOWLClass(URI.create(ontologyURI + "#Relex_word"));

			//Generic properties for classes
			has = factory.getOWLObjectProperty(URI.create(ontologyURI + "#relex_has"));
			//map_owl_properties.put("type",factory.getOWLObjectProperty(URI.create(ontologyURI + "#type")));

			// word is subclass of phrase
			//OWLAxiom subclassing = factory.getOWLSubClassAxiom(word, sentence);

			// Add the axiom to the ontology
			//AddAxiom addAxiom = new AddAxiom(ontology, subclassing);
			// We now use the manager to apply the change
			//manager.applyChange(addAxiom);

			//2º Add the predefined properties

			/*map_owl_properties.put("tense",factory.getOWLObjectProperty(URI.create(ontologyURI + "#p_tense")));
			map_owl_properties.put("index",factory.getOWLDataProperty(URI.create(ontologyURI + "#p_index")));
			//Add possible relex words
			map_owl_relexwords.put("masculine",factory.getOWLIndividual(URI.create(ontologyURI + "#rw_masculine")));
			map_owl_relexwords.put("feminine",factory.getOWLIndividual(URI.create(ontologyURI + "#rw_feminine")));
			map_owl_relexwords.put("person",factory.getOWLIndividual(URI.create(ontologyURI + "#rw_person")));
			map_owl_relexwords.put("neuter",factory.getOWLIndividual(URI.create(ontologyURI + "#rw_neuter")));*/

			/*OWLObjectProperty number = factory.getOWLObjectProperty(URI.create(ontologyURI + "#number"));
			OWLObjectProperty tense = factory.getOWLObjectProperty(URI.create(ontologyURI + "#tense"));
			OWLObjectProperty query = factory.getOWLObjectProperty(URI.create(ontologyURI + "#query"));
			OWLObjectProperty quantification = factory.getOWLObjectProperty(URI.create(ontologyURI + "#quantification"));*/

			// Add axioms to the ontology
			//OWLAxiom genderax = factory.getOWLObjectProperty(infinitive);

			//Phrase Individuals
			/*phr_type_map_owl = new HashMap<String,OWLIndividual>();
			phr_type_map_owl.put("Adverbial Phrase",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_adjective")));
			phr_type_map_owl.put("Adverbial Phrase",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_adverb")));
			phr_type_map_owl.put("Noun Phrase",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_noun")));
			phr_type_map_owl.put("Prepositional Phrase",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_prepositional")));
			phr_type_map_owl.put("Particle",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_particle")));
			phr_type_map_owl.put("Quantifier Phrase",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_quantifier")));
			phr_type_map_owl.put("Clause",		factory.getOWLIndividual(URI.create(ontologyURI + "#rw_clause")));
			phr_type_map_owl.put("Subordinate Clause",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_subordinate")));
			phr_type_map_owl.put("Subject Inverted",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_inverted")));
			phr_type_map_owl.put("Sentence",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_root")));
			phr_type_map_owl.put("Verb Phrase",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_verb")));
			phr_type_map_owl.put("Wh-Adverb Phrase", factory.getOWLIndividual(URI.create(ontologyURI + "#rw_wh-adverb")));
			phr_type_map_owl.put("Wh-Noun Phrase",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_wh-noun")));
			phr_type_map_owl.put("Wh-Prepositional Phrase",	factory.getOWLIndividual(URI.create(ontologyURI + "#rw_wh-prep")));*/

			//Add all the phr_type_map_owl Individuals to the Relex_word Class
			/*Set<String> s = phr_type_map_owl.keySet();
			for (Iterator<String> it = s.iterator(); it.hasNext();)
			{
				manager.applyChange(new AddAxiom(ontology, factory.getOWLClassAssertionAxiom(phr_type_map_owl.get(it.next()), relex_word)));
			}

			//Add all the map_owl_relexwords Individuals to the Relex_word Class
			s = map_owl_relexwords.keySet();
			for (Iterator<String> it = s.iterator(); it.hasNext();)
			{
				manager.applyChange(new AddAxiom(ontology, factory.getOWLClassAssertionAxiom(map_owl_relexwords.get(it.next()), relex_word)));*/
			}

		}
		catch (OWLException e)
		{
			e.printStackTrace();
		}
	}


	public void printRelations(ParsedSentence parse, HashMap<FeatureNode,String> map)
	{
		Visit v = new Visit();
		v.id_map = map;
		v.str = "";
		parse.foreach(v);
	}

	public void saveOWL(String path)
	{
		try
		{
			// Save the ontology
			physicalURI = URI.create("file:" + path);

			manager.setPhysicalURIForOntology(ontology, physicalURI);

			manager.saveOntology(ontology);
		}
		catch (OWLException ex)
		{
			Logger.getLogger(ParseView.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	* Print out RelEx relations, alternate format.
	* Unary relations, including booleans, doen't show
	* the attribute name.
	*
	* Example:
	*	_subj(throw, John)
	*	_obj(throw, ball)
	*	past(throw)
	*	definite(ball)
	*	singular(ball)
	*/
	public String printRelationsAlt(ParsedSentence parse)
	{
		return printRelationsAlt(parse, null);
	}

	public String printRelationsAlt(ParsedSentence parse, HashMap<FeatureNode,String> map)
	{
		Visit v = new Visit();
		v.id_map = map;
		v.unaryStyle = true;
		v.str = "";
		parse.foreach(v);

		return v.str;
	}

	private class Visit implements RelationCallback
	{
		// Map associating a feature-node to a unique ID string.
		public HashMap<FeatureNode,String> id_map = null;

		public Boolean unaryStyle = false;
		public String str;
		public Boolean BinaryHeadCB(FeatureNode node) { return false; }

		public Boolean BinaryRelationCB(String relName,
												FeatureNode srcNode,
												FeatureNode tgtNode)
		{
			try
			{
				String srcName = srcNode.get("name").getValue();
				FeatureNode tgt = tgtNode.get("name");
				if (tgt == null)
				{
					System.out.println("Error: No target! rel=" + relName + " and src=" + srcName);
					return false;
				}
				String tgtName = tgt.getValue();
				if (id_map != null)
				{
					srcName = id_map.get(srcNode);
					tgtName = id_map.get(tgtNode);
				}
				//Optimize using StringBuilder
				//Get the Individual type (noun, etc.) and the type_property
				System.out.println("\n\tRELATION (binary) = " + relName + "(" + srcName + ", " + tgtName + ")\n");

				//Cleaning
				if(relName.charAt(0)=='_')
					relName = relName.replaceFirst("_", "");
					if(relName.length()>1)
							if(relName.charAt(1)=='%')
								relName = relName.replaceFirst("%", "");

				if(tgtName.contains("[") || tgtName.contains("]") || srcName.contains("[") || srcName.contains("]") ||
							tgtName.equals("WORD") || srcName.equals("WORD")  || tgtName.contains("misc-") || srcName.contains("misc-")) return false;

				if(srcName.length()>0 && tgtName.length()>0)
				{
					//1º Add the first term to Word Class
					srcName=srcName.replaceAll("[\\%\\s]", "");
					//System.out.println("srcName = " + srcName);
					OWLIndividual src_word = factory.getOWLIndividual(URI.create(ontologyURI + "#" + srcName));
					OWLClassAssertionAxiom addSrcWord = factory.getOWLClassAssertionAxiom(src_word, word);
					manager.applyChange(new AddAxiom(ontology, addSrcWord));

					//2º Create the property
					relName = relName.replaceAll("[\\%\\ss]", "");
					OWLObjectProperty rel = factory.getOWLObjectProperty(URI.create(ontologyURI + "#" + relName));

					//3º Add the second term to Word Class
					tgtName = tgtName.replaceAll("[\\%\\s]", "");
					OWLIndividual dst_word = factory.getOWLIndividual(URI.create(ontologyURI + "#" + tgtName));
					OWLClassAssertionAxiom addDstWord = factory.getOWLClassAssertionAxiom(dst_word, word);
					manager.applyChange(new AddAxiom(ontology, addDstWord));

					//4º Create axiom for the relation
					OWLObjectPropertyAssertionAxiom addrel = factory.getOWLObjectPropertyAssertionAxiom(src_word, rel, dst_word);
					manager.applyChange(new AddAxiom(ontology, addrel));

					//5º Add the words (Class) to the sentence (Class)
					OWLObjectPropertyAssertionAxiom addw1 = factory.getOWLObjectPropertyAssertionAxiom(sentenceInd, has, src_word);
					//OWLObjectPropertyAssertionAxiom addw2 = factory.getOWLObjectPropertyAssertionAxiom(sentenceInd, has, dst_word);


					manager.applyChange(new AddAxiom(ontology, addw1));
					//manager.applyChange(new AddAxiom(ontology, addw2));
				}

			}
			catch (OWLException ex)
			{
				Logger.getLogger(OWLView.class.getName()).log(Level.SEVERE, null, ex);
			}
			return false;
		}

		public Boolean UnaryRelationCB(FeatureNode srcNode, String attrName)
		{
			FeatureNode attr = srcNode.get(attrName);
			if (!attr.isValued()) return false;
			String value = attr.getValue();
			String srcName = srcNode.get("name").getValue();

			if (id_map != null)
			{
				srcName = id_map.get(srcNode);
			}
			if (unaryStyle)
			{
				if (attrName.endsWith("-FLAG"))
					value = attrName.replaceAll("-FLAG","").toLowerCase();

				if (attrName.equals("HYP"))
					value = attrName.toLowerCase();

				try
				{
					//Optimize using StringBuilder
					//Get the Individual type (noun, etc.) and the type_property
					System.out.println("\n\tRELATION (unary1) = " + value + "(" + srcName + ")\n");

					//1º Add the first term to Word Class
					srcName=srcName.replaceAll("\\%", "");
					OWLIndividual src_word = factory.getOWLIndividual(URI.create(ontologyURI + "#" + srcName.replaceAll(" ", "")));
					OWLClassAssertionAxiom addSrcWord = factory.getOWLClassAssertionAxiom(src_word, word);
					manager.applyChange(new AddAxiom(ontology, addSrcWord));

					//2º Create the property
					OWLObjectProperty rel = factory.getOWLObjectProperty(URI.create(ontologyURI + "#relex_is"));

					//3º Add the second term to Word Class
					value=value.replaceAll("\\%", "");
					OWLIndividual dst_word = factory.getOWLIndividual(URI.create(ontologyURI + "#" + value.replaceAll(" ", "")));
					OWLClassAssertionAxiom addDstWord = factory.getOWLClassAssertionAxiom(dst_word, word);
					manager.applyChange(new AddAxiom(ontology, addDstWord));

					//4º Create axiom for the relation
					OWLObjectPropertyAssertionAxiom addrel = factory.getOWLObjectPropertyAssertionAxiom(src_word, rel, dst_word);
					manager.applyChange(new AddAxiom(ontology, addrel));

					//5º Add the words (Class) to the sentence (Class)
					/*OWLObjectPropertyAssertionAxiom addw1 = factory.getOWLObjectPropertyAssertionAxiom(sentenceInd, has, src_word);
					OWLObjectPropertyAssertionAxiom addw2 = factory.getOWLObjectPropertyAssertionAxiom(sentenceInd, has, dst_word);

					manager.applyChange(new AddAxiom(ontology, addw1));
					manager.applyChange(new AddAxiom(ontology, addw2));*/

				}
				catch (OWLException ex)
				{
					Logger.getLogger(OWLView.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			else
			{
				try
			 	{
					//Optimize using StringBuilder
					//Get the Individual type (noun, etc.) and the type_property
					System.out.println("\n\tRELATION (unary2) = " + attrName + "(" + srcName + ", " + value + ")\n");

					if(value.charAt(0)=='.')
						value = value.replaceFirst(".", "");

					if(value.contains("[") || value.contains("]") || srcName.contains("[") || srcName.contains("]") ||
								value.equals("WORD") || srcName.equals("WORD") || value.contains("misc-") || srcName.contains("misc-")) return false;

					if(srcName.length()>0 && value.length()>0)
					{
						//1º Add the first term to Word Class
						srcName=srcName.replaceAll("[\\%\\s]", "");
						OWLIndividual src_word = factory.getOWLIndividual(URI.create(ontologyURI + "#" + srcName));
						OWLClassAssertionAxiom addSrcWord = factory.getOWLClassAssertionAxiom(src_word, word);
						manager.applyChange(new AddAxiom(ontology, addSrcWord));

						//2º Create the property
						attrName=attrName.replaceAll("[\\%\\s]", "");
						OWLObjectProperty rel = factory.getOWLObjectProperty(URI.create(ontologyURI + "#" + attrName));

						//3º Add the second term to Word Class
						value=value.replaceAll("[\\%\\s]", "");
						OWLIndividual dst_word = factory.getOWLIndividual(URI.create(ontologyURI + "#" + value));
						OWLClassAssertionAxiom addDstWord = factory.getOWLClassAssertionAxiom(dst_word, word);
						manager.applyChange(new AddAxiom(ontology, addDstWord));

						//4º Create axiom for the relation
						OWLObjectPropertyAssertionAxiom addrel = factory.getOWLObjectPropertyAssertionAxiom(src_word, rel, dst_word);
						manager.applyChange(new AddAxiom(ontology, addrel));

						//5º Add the words (Class) to the sentence (Class)
						OWLObjectPropertyAssertionAxiom addw1 = factory.getOWLObjectPropertyAssertionAxiom(sentenceInd, has, src_word);
						//OWLObjectPropertyAssertionAxiom addw2 = factory.getOWLObjectPropertyAssertionAxiom(sentenceInd, has, dst_word);

						manager.applyChange(new AddAxiom(ontology, addw1));
						//manager.applyChange(new AddAxiom(ontology, addw2));
					}

				}
				catch (OWLException ex)
				{
					Logger.getLogger(OWLView.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			return false;
		}
	}
}

/* ============================ END OF FILE ====================== */


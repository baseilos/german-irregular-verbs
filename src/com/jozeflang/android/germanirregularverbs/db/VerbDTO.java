package com.jozeflang.android.germanirregularverbs.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/** 
 * A word data transfer object (DTO object).
 * @author Jozef Lang (developer@jozeflang.com)
 */
public class VerbDTO {
	 
	private final int id;
	private final String present;
	private final Collection<Translation> translations;
	private final Collection<Perfect> perfects;
	private final Collection<Preterite> preterites;
	
	private VerbDTO(int id, String present) {
		this.id = id;
		this.present = present;
		translations = new ArrayList<Translation>();
		perfects = new ArrayList<Perfect>();
		preterites = new ArrayList<Preterite>();
	}
	
	public int getId() {
		return id;
	}
	
	public String getPresent() {
		return present;
	}
	
	void addTranslation(String translation) {
		translations.add(new Translation(translation));
	}
	
	public Collection<Translation> getTranslations() {
		return Collections.unmodifiableCollection(translations);
	}
	
	void addPerfect(String auxVerb, String perfect) {
		perfects.add(new Perfect(auxVerb, perfect));
	}
	
	public Collection<Perfect> getPerfects() {
		return Collections.unmodifiableCollection(perfects);
	}
	
	void addPreterite(String preterite) {
		preterites.add(new Preterite(preterite));
	}
	
	public Collection<Preterite> getPreterites() {
		return Collections.unmodifiableCollection(preterites);
	}
	
	static VerbDTO of(int id, String present) {
		 return new VerbDTO(id, present);
	 }
	
	/**
	 * @author Jozef Lang (developer@jozeflang.com)
	 */
	public class Translation {
		
		private final String translation;
		
		private Translation(String translation) {
			this.translation = translation;
		}
		
		public String getTranslation() {
			return translation;
		}
		
	}
	
	/**
	 * @author Jozef Lang (developer@jozeflang.com)
	 */
	public class Preterite {
		
		private final String preterite;
		
		private Preterite(String preterite) {
			this.preterite = preterite;
		}
		
		public String getPreterite() {
			return preterite;
		}
		
	}

	/**
	 * @author Jozef Lang (developer@jozeflang.com)
	 */
	public class Perfect {
	
		private final String auxVerb;
		private final String perfect;
		
		private Perfect(String auxVerb, String perfect) {
			this.auxVerb = auxVerb;
			this.perfect = perfect;
		}
		
		public String getAuxVerb() {
			return auxVerb;
		}
		
		public String getPerfect() {
			return perfect;
		}
		
	}
	
}

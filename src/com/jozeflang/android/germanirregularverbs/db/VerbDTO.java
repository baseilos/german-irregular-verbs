package com.jozeflang.android.germanirregularverbs.db;

import android.content.ContentValues;

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
    private final boolean active;
	private final Collection<Translation> translations;
	private final Collection<Perfect> perfects;
	private final Collection<Preterite> preterites;
	
	private VerbDTO(int id, String present, boolean active) {
		this.id = id;
		this.present = present;
        this.active = active;
		translations = new ArrayList<Translation>();
		perfects = new ArrayList<Perfect>();
		preterites = new ArrayList<Preterite>();
    }

    private VerbDTO(VerbDTO verb, boolean isActive) {
        this.id = verb.id;
        this.present = verb.present;
        this.active = isActive;
        this.translations = new ArrayList<Translation>();
        for (Translation t : verb.translations) {
            this.translations.add(new Translation(t));
        }
        this.perfects = new ArrayList<Perfect>();
        for (Perfect p : verb.perfects) {
            this.perfects.add(new Perfect(p));
        }
        this.preterites = new ArrayList<Preterite>();
        for (Preterite p : verb.preterites) {
            this.preterites.add(new Preterite(p));
        }
    }

	public int getId() {
		return id;
	}
	
	public String getPresent() {
		return present;
	}

    public boolean isActive() { return active; }

    public VerbDTO switchActive() {
        VerbDTO newVerb = new VerbDTO(this, !active);
        return newVerb;
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
	
	static VerbDTO of(int id, String present, int active) {
		 return new VerbDTO(id, present, active == 1);
	 }
	
	/**
	 * @author Jozef Lang (developer@jozeflang.com)
	 */
	public class Translation {
		
		private final String translation;
		
		private Translation(String translation) {
			this.translation = translation;
		}

        private Translation(Translation translation) {
            this.translation = translation.translation;
        }
		
		public String getTranslation() {
			return translation;
		}

        @Override
        public String toString() {
            return getTranslation();
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

        private Preterite(Preterite preterite) {
            this.preterite = preterite.preterite;
        }
		
		public String getPreterite() {
			return preterite;
		}

        @Override
        public String toString() {
            return getPreterite();
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

        private Perfect(Perfect perfect) {
            this.auxVerb = perfect.auxVerb;
            this.perfect = perfect.perfect;
        }
		
		public String getAuxVerb() {
			return auxVerb;
		}
		
		public String getPerfect() {
			return perfect;
		}

        @Override
        public String toString() {
            return getAuxVerb() + " " + getPerfect();
        }
		
	}
	
}

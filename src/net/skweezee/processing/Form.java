package net.skweezee.processing;

import java.util.*;

/**
 * Class to describe and handle a static form, including methods to record and recognize it.
 * 
 * @author Bert Vandenberghe @ eMedia Research Lab, KU Leuven
 */
public class Form {
	
	ArrayList<float[]> set;
	String label;
	
	/**
	 * Form class constructor.
	 * 
	 * @param sample
	 */
	public Form(float[] sample) {
		set = new ArrayList<float[]>();
		set.add(sample);
	}
	
	/**
	 * Form class constructor.
	 * 
	 * @param sample
	 */
	public Form(Vector sample) {
		set = new ArrayList<float[]>();
		set.add(sample.array());
	}
	
	
	/**
	 * Form class constructor.
	 */
	public Form() {
		set = new ArrayList<float[]>();
		label = "";
	}
	
	
	/**
	 * Form class constructor.
	 * 
	 * @param label
	 */
	public Form(String label) {
		set = new ArrayList<float[]>();
		this.label = label;
	}
	
	/**
	 * Form class constructor.
	 * 
	 * @param label
	 * @param sample
	 */
	public Form(String label, float[] sample) {
		set = new ArrayList<float[]>();
		set.add(sample);
		this.label = label;
	}
	
	/**
	 * Form class constructor.
	 * 
	 * @param label
	 * @param sample
	 */
	public Form(String label, Vector sample) {
		set = new ArrayList<float[]>();
		set.add(sample.array());
		this.label = label;
	}
	
	/**
	 * Record sample. Overwrites existing sample (or set of samples).
	 * 
	 * @param template
	 */
	public void rcd(float[] sample) {
		set = new ArrayList<float[]>();
		set.add(sample);
	}
	
	/**
	 * Record sample. Overwrites existing sample (or set of samples).
	 * 
	 * @param template
	 */
	public void rcd(Vector sample) {
		set = new ArrayList<float[]>();
		set.add(sample.array());
	}
	
	/**
	 * Record synonym, adds specified sample to set of samples.
	 * 
	 * @param sample
	 */
	public void add(float[] sample) {
		set.add(sample);
	}
	
	/**
	 * @return label
	 */
	public String label() {
		return label;
	}
	
	/**
	 * Check whether form label corresponds with specified label.
	 * 
	 * @param label
	 * @return match
	 */
	public Boolean is(String label) {
		return this.label.equals(label);		
	}
	
	/**
	 * 
	 * @return fit
	 */
	public float rcg() {
		return rcg(Skweezee.vector());
	}
	
	/**
	 * 
	 * @return fit
	 */
	public float rcg(float[] v) {
		float fit = 0;
		for(int i = 0; i < set.size(); i++) {
			float dot = Vector.dot(set.get(i), v);
			if(dot > fit) {
				fit = dot;
			}	
		}
		return fit;
	}
	
}

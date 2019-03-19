package net.skweezee.processing;


/**
 * Class to describe and handle multidimensional vectors, including methods for basic 
 * vector calculus.
 * 
 * @author Bert Vandenberghe @ eMedia Research Lab, KU Leuven
 */
public class Vector {
	
	// vector as array of floats
	private float[] vector;
	private int n;
	
	/**
	 * Vector constructor. Creates Vector object from array of floats.
	 * 
	 * @param array
	 */
	public Vector(float[] array) {	
		this.vector = array;
		n = array.length;
	}
	
	
	/**
	 * Vector as array of floats.
	 * 
	 * @return array
	 */
	public float[] array() {
		return vector;
	}
	
	/**
	 * Vector component at specified position.
	 * 
	 * @param i  position
	 * @return vector component
	 */
	public float x(int i) {	
		return vector[i];
	}
	
	/**
	 * Vector dimension.
	 * 
	 * @return dimension
	 */
	public int dim() {
		return n;
	}
	
	
	/**
	 * Vector magnitude (length of the vector).
	 * 
	 * @return magnitude
	 */
	public float mag() {
		float mag = 0;
		for(int i = 0; i < n; i++) {
			mag += vector[i]*vector[i];
		}
		return (float) Math.sqrt(mag);
	}
	
	
	/**
	 * Vector magnitude (length of the vector).
	 * 
	 * @return magnitude
	 */
	static public float mag(float[] v) {
		return new Vector(v).mag();
	}
	
	
	/**
	 * Vector direction (unit vector).
	 * 
	 * @return direction
	 */
	public float[] dir() {
		float[] dir = new float[n];
		float mag = mag();
		for(int i = 0; i < n; i++) {
			dir[i] = vector[i]/mag;
		}
		return dir;
	}
	
	/**
	 * Vector direction (unit vector).
	 * 
	 * @return direction
	 */
	static public float[] dir(float[] v) {
		return new Vector(v).dir();
	}
	
	
	/**
	 * Dot product (scalar product) between this and other vector, relates to 
	 * the angle between the two vectors.
	 * 
	 * @return dot product (scalar)
	 */
	public float dot(Vector other) {
		float dot = 0;
		if(other.dim() == this.dim()) {
			float[] u = this.dir();
			float[] v = other.dir();
			for(int i = 0; i < dim(); i++) {
				dot += u[i]*v[i];
			}
		}
		return dot;
	}

	
	/**
	 * Dot product (scalar product) between this and other vector, relates to 
	 * the angle between the two vectors.
	 * 
	 * @return dot product (scalar)
	 */
	public float dot(float[] other) {
		return dot(new Vector(other));
	}
	
	
	/**
	 * Dot product (scalar product) between two vectors, relates to 
	 * the angle between the two vectors.
	 * 
	 * @return dot product (scalar)
	 */
	static public float dot(float[] one, float[] other) {
		return new Vector(one).dot(new Vector(other));
	}

}

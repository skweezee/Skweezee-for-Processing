package net.skweezee.processing;

import java.util.*;
import processing.core.*;
import processing.serial.*;


/**
 * The Skweezee class connects physical and digital components of a Skweezee.
 * The library connects to the Arduino microcontroller (with or without Skweezee 
 * shield) and to the Processing environment. It senses physical form over time  
 * (deformation) and offers data processing methods.
 * 
 * The retrieved raw data is pre-processed (scaled, split) and represented as
 * a continuously updated vector. Now, the deformation data can be processed
 * for feature extraction based on standard vector analysis (for example vector
 * magnitude as measure for amplitude or a dot product for form recognition) as
 * well as analysis of historical data (such as a moving average for smoothing 
 * or moving standard deviation for signal stability). Basic calculus offers 
 * ways to nuance Skweezee signals (for example, a square root emphasizes small 
 * deformations, where a square emphasizes big deformations).
 * 
 * This class connects an Arduino to the Processing environment, reads its data,
 * performs data processing: pre-processing (scaling, splitting), vector
 * analysis (magnitude, direction, dot product), statistical analysis of
 * historical data (moving average, moving standard deviation), and form
 * recognition.
 * 
 * As this class implements the singleton design pattern, its instance can be
 * accessed directly from the parent Processing Sketch. Check the examples to
 * explore its possibilities.
 * 
 * More info: skweezee.net
 *
 * @author Bert Vandenberghe @ eMedia Research Lab, KU Leuven
 */
public class Skweezee {
	
	/* 
	 * Skweezee implements the singleton design pattern, restricting the
	 * instantiation of the Skweezee class to one object. The Skweezee instance
	 * is created in the line below, and can be accessed directly from the
	 * parent Processing sketch.
     */
	static private final Skweezee instance = new Skweezee();

	/**
	 * Version
	 */
	static public final String VERSION = "1.0.0";
	
	/**
	 * Labels of values in a measurement and components of its corresponding
	 * vector, which can be used to identify a single value or vector component.
	 * This map is only relevant when using the Skweezee shield for Arduino.
	 * 
	 * LABELS contains 28 labels (Strings) of the values at the corresponding
	 * position in a measurement or components of the corresponding vector. For
	 * example the eleventh value in a measurement originates from the pair
	 * labeled by LABELS[11] ("1-6"). 
	 */
	static public final String[] LABELS = { "0-1", "0-2", "0-3", "0-4",
			 "0-5", "0-6", "0-7", "1-2", "1-3", "1-4", "1-5", "1-6", "1-7",
			 "2-3", "2-4", "2-5", "2-6", "2-7", "3-4", "3-5", "3-6", "3-7",
			 "4-5", "4-6", "4-7", "5-6", "5-7", "6-7" };
	
	/**
	 * Indices of values in a measurement per electrode, which can be used
	 * to retrieve information per electrode. This map is only relevant when
	 * using the Skweezee shield for Arduino.
	 * 
	 * SUBVECTOR_MAP[0] contains all indices of values in a measurement
	 * involving the first electrode 0, so pointing to the measured values
	 * {index_of_0-1, index_of_0-2, ..., index_of_0-7}.
	 * 
	 * SUBVECTOR_MAP contains 8x7 indices from 0 to 27.
	 * 
	 * Note that accessing the values in this way introduces a duplication of
	 * data. Each value can be accessed twice: for example SUBVECTOR_MAP[3][4]
	 * and SUBVECTOR_MAP[5][3] contain the index of the same value (the index
	 * of pair {3-5}: 20) 
	 */
	static public final int[][] SUBVECTOR_MAP = {{0, 1, 2, 3, 4, 5, 6}, {0, 7, 8, 9, 10, 11, 12}, {1, 7,
		13, 14, 15, 16, 17}, {2, 8, 13, 18, 19, 20, 21}, {3, 9, 14, 18, 22, 23, 24}, {4, 10, 15, 19, 22, 25,
		26}, {5, 11, 16, 20, 23, 25, 27}, {6, 12, 17, 21, 24, 26, 27}};
	
	/* 
	 * Parent Processing sketch, which calls methods and retrieves data from
	 * this library, for example to implement computed Skweezee feedback.
	 */
	private PApplet parent;
	
	/* 
	 * Variables to setup a serial connection and read data from Arduino.
	 */
	private	Serial port;  // serial port
	private boolean started;  // boolean used to find the starting point
	private boolean running;
	private boolean shield;
	private List<Integer> buffer;  // buffer used to record a single cycle
	
	/* Skweezee variables, used to store the sensed and intermediate computed
	 * data. These variables are updated each cycle, other functionality and
	 * features are calculated on request (so not every cycle).
	 */
	private int[] raw;  // set of raw values in one sensing cycle
	private int dim;  // number of values in one measurement
	private Vector vector;  // squeeze vector, based on measurement
	private Vector[] window;  // used to store a time series of vectors
	private float mag;  // momentary magnitude of vector
	private float max;  // maximum observed magnitude of vector
	private float[] submag;  // momentary magnitudes per electrode
	private float[] submax;  // maximum observed magnitudes per electrode
	private List<Form> forms;  // stored form samples, used for form recognition

	
/* CONSTRUCTOR */ 		
	
	/*
	 * Private constructor (singleton), initializes variables.
	 */
	private Skweezee() {
		port = null;
		started = false;
		running = false;
		shield = false;
		buffer = new ArrayList<Integer>();
		raw = new int[1];
		dim = 0;
		vector = new Vector(new float[1]);
		window = new Vector[5];
		mag = 0;
		max = 0;
		submag = new float[8];
		submax = new float[8];
		forms = new ArrayList<Form>();
	}
	
	
/* CONNECTION */	
	
	/**
	 * Connects Skweezee instance to parent Processing sketch and USB (Arduino).
	 * 
	 * NOTE: this implementation looks for the right serial port automatically,
	 * this works on Mac only. On Windows (or for manual connection, use
	 * connect(PApplet parent, int port)).
	 * 
	 * Call this method in Processing with <code>Skweezee.connect(this)</code>
	 * 
	 * @param parent  Processing sketch (‘this’ in Processing).
	 * @see connect(PApplet, int)
	 */
	static public void connect(PApplet parent) {
		// parent Processing sketch
		instance.parent = parent;  // TODO multiple parents or Skweezees?
		// creates hooks with Processing sketch, 'pre' is called at the begin
		// of every draw cycle, 'dispose' is called when the Processing sketch
		// is terminated.
		instance.parent.registerMethod("draw", instance);
		instance.parent.registerMethod("dispose", instance);
		// connect USB
		if (parent != null) {
			// if parent exists
			int index = -1;
			for (int i = 0; i < Serial.list().length; i++) {
				// for every available serial port, check if its name contains
				// 'tty.usbmodem'
				if (Serial.list()[i].contains("tty.usbmodem")) {
					// name matches required pattern: remember index
					index = i;
					break;
				}
			}
			// check if matching port name is found
			if (index != -1) {
				// if a matching port name was found, index is known
				// setup serial connection & clear port
				instance.port = new Serial(parent, Serial.list()[index], 9600);
				instance.port.clear();
				System.out.println("USB Connected");
			} else {
				// if no matching port name found, index unknown
				System.out.println("Error: unable to connect (serial port not found)");
			}
		} else {
			// if no parent
			System.out.println("Error: unable to connect (parent sketch not assigned)");
		}
	}
	
	
	/**
	 * Connects Skweezee instance to parent Processing sketch and USB (Arduino).
	 * Note that this implementation looks for the right serial port automatically,
	 * this works on Mac only. On Windows (or for manual connection, use connect(PApplet parent, int port)).
	 * 
	 * Call this method in Processing with <code>Skweezee.connect(this)</code>
	 * 
	 * @param parent  Parent processing sketch (‘this’ in Processing).
	 * @param port  Serial port.
	 * @see connect(PApplet)
	 */
	static public void connect(PApplet parent, int port) {
		// parent Processing sketch
		instance.parent = parent;
		// TODO multiple parents or Skweezees?
		// creates hooks with Processing sketch, 'pre' is called at the beginning
		// of every draw cycle, 'dispose' is called when the Processing sketch is closed.
		instance.parent.registerMethod("draw", instance);
		instance.parent.registerMethod("dispose", instance);
		// connect USB
		if (parent != null) {
			// parent exists
			if (port < Serial.list().length) {
				// a matching port name was found, index is known
				// setup serial connection & clear port
				instance.port = new Serial(parent, Serial.list()[port], 9600);
				instance.port.clear();
				System.out.println("USB Connected");
			} else {
				// no matching port name found, index unknown
				System.out.println("Error: unable to connect (serial port not found)");
			}
		} else {
			// no parent
			System.out.println("Error: unable to connect (parent sketch not assigned)");
		}
	}
	
	/*
	 * Disconnects serial port
	 */
	private void disconnect() {
		if (port != null) {
			port.clear();
			port.stop();
			port = null;
			System.out.println("USB disconnected");
		} else {
			System.out.println("Error: unable to disconnect (serial port not connected)");	
		}
	}
	
	
	/**
	 * Hook with the parent Processing sketch. This method is called at the beginning
	 * of every draw cycle of the sketch, meaning it can affect drawing.
	 * 
	 * Skweezee senses the deformation, computes its corresponding vector representation
	 * and performs basic analysis (meaning the minimum set of features that is required to be
	 * updated every cycle), and stores a sliding window of the squeeze vector. Further analysis
	 * is performed on request through the corresponding methods.
	 */
	public void draw() {
		sense();
		checkStatus();
		prep();
		basic();
		store();
	}
	
	/**
	 * Hook with the parent Processing sketch. This method is called when the parent
	 * sketch is shutting down.
	 */
	public void dispose() {
		disconnect();
	}
	

/* SKWEEZEE FUNCTIONALITY: SENSING & COMPUTING */	
	
	private void checkStatus() {
		if(started) {
			if(dim > 0) {
				running = true;
				if(dim == 28) {
					shield = true;
				}
			}
		}
	}
	
	/*
	 * Sense form.
	 */
	private void sense() {
		if (port != null) {
			// if connected
			// check number of available bytes
			int n = port.available();
			if (n > 0) {
				// if data available
				// create byte array that fits the available data
				// and write the available data to that byte array
				byte[] u = new byte[n];
				port.readBytes(u);
				for (int v = 0; v < u.length; v++) {
					// for each element in the byte array
					// cast array element (byte) to integer
					int t = (int) u[v] & 0xff;
					if (!started) {
						// if the first 'flag' has not been found
						if (t == 0) {
							// if the current element equals '0' (a flag)
							// start reading data, create empty arrayList to store all data until next flag
							started = true;
							buffer = new ArrayList<Integer>();
						}
					} else {
						// if the first flag has been found, and reading has started
						if (t == 0) {
							// if current element equals '0' (a flag)
							dim = buffer.size();
							// assign new array to measurement,
							// with a size equal to the number of elements in the buffer
							raw = new int[dim];
							for (int i = 0; i < dim; i++) {
								// copy each element in the buffer to the measurement
								raw[i] = buffer.get(i);	
							}
							// clear buffer to start over
							buffer = new ArrayList<Integer>();
						} else {
							// if the current element does not equal '0' > add element to buffer
							buffer.add(t);
						} 
					} 
				} 
			}
		} else {
			// if not connected	
		}
	}
	
	/*
	 * Vectorize data.
	 */
	private void prep() {
		if (running) {
			// if measurement contains actual data
			// due to the pull-up resistors, an actual value can never be 0
			// assign new array of corresponding length to vector
			float[] x = new float[raw.length];
			for (int i = 0; i < raw.length; i++) {
				// copy each measurement element to vector
				// after inverting (255 - element) and scaling (/255)
				// the resulting vector element is a number from 0 (min squeeze) to 1 (max squeeze)
				x[i] = (255-(float) raw[i])/255;
			}
			vector = new Vector(x);
		}
	}
	
	
	/*
	 * Basic analysis, key parameters which are calculated continuously, as 
	 * other parameters depend on it. Other parameters are calculated on demand.
	 */
	private void basic() {
		if (running) {
			mag = vector.mag();
			if (mag > max) {
				max = mag;
			}
			if(shield) {
				submag = new float[8];
				for(int i = 0; i < 8; i++) {
					submag[i] = subvector(vector, i).mag();
					if(submag[i] > submax[i]) {
						submax[i] = submag[i];
					}
				}
			}
		}
	}

	
	/*
	 * feed sliding window (stores momentary vector for later analysis of
	 * historic data).
	 */
	private void store() {
		if(running) {
			window[4] = window[3];
			window[3] = window[2];
			window[2] = window[1];
			window[1] = window[0];
			window[0] = vector;
		}
	}
	
	
/*	VECTOR ANALYSIS */
	
	/**
	 * Skweezee dimension meaning the number of values 
	 * 
	 * In a correct functioning Skweezee, this value should not change during 
	 * runtime—in case the physical components stay unchanged.
	 * 
	 * @return dimension
	 */
	public static int dim() {
		return instance.dim;
	}
	
	/**
	 * Raw values as measured by the Arduino in one cycle. For each of the 
	 * existing pairs of electrodes, the electrical resistance between the 
	 * elecrodes is measured and represented by a number in the range 0–255. 
	 * These values are grouped per cycle (one cycle meaning the measurement of 
	 * all unique electrode pairs).
	 * 
	 * Note that a bigger value means a bigger measured voltage, as a result of 
	 * a bigger electrical resistance (this inverse is caused by the voltage
	 * divider). A bigger electrical resistance means a larger distance, and 
	 * thus a smaller deformation. In short, large number = small deformation.
	 * 
	 * The raw method returns the momentary measured values. It is recommended 
	 * to call this method continuously, for example in the draw() function of 
	 * the parent Processing sketch, as this function is executed continuously.
	 * This implementation will result in a continuous signal.
	 * 
	 * @return momentary raw measurement
	 * @see raw(int)
	 * @see dim()
	 * @see vector()
	 */
	public static int[] raw() {
		return instance.raw;
	}
	
	/**
	 * Subset of raw values as measured by the Arduino in one cycle, related to 
	 * the specified electrode. The subset contains the measured electrical 
	 * resistance from the specified electrode to all other electrodes. This 
	 * electrical resistance is represented by a number in the range 0–255. 
	 * These values are grouped per cycle (one cycle meaning the measurement of 
	 * all unique electrode pairs).
	 * 
	 * Note that a bigger value means a bigger measured voltage, as a result of 
	 * a bigger electrical resistance (this inverse is caused by the voltage
	 * divider). A bigger electrical resistance means a larger distance, and 
	 * thus a smaller deformation. In short, large number = small deformation.
	 * 
	 * The raw method returns the momentary measured values. It is recommended 
	 * to call this method continuously, for example in the draw() function of 
	 * the parent Processing sketch, as this function is executed continuously.
	 * This implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary raw measurement
	 * @see raw()
	 * @see dim()
	 * @see vector(int)
	 */
	public static int[] raw(int e) {
		if(instance.shield) {
			int[] m = new int[] {0, 0, 0, 0, 0, 0, 0};
			for(int i = 0; i < 7; i++) {
				if(instance.raw.length >= 28) {
					m[i] = instance.raw[SUBVECTOR_MAP[e][i]];
				}
			}
			return m;
		}
		return new int[] {0};
	}
	
	
	/**
	 * Sensed deformation presented as a multidimensional vector. The vector is 
	 * constructed from the pre-processed raw measurement. All values are scaled 
	 * to a range 0–1 and reversed. Each vector component represents the
	 * measured voltage (thus electrical resistance, thus distance, thus
	 * deformation).
	 * 
	 * Note that a bigger value means a bigger deformation. A bigger value is 
	 * derived from a smaller measured voltage, as a result of a bigger
	 * electrical resistance (this inverse is caused by the voltage divider). 
	 * A bigger electrical resistance means a larger distance, and thus a
	 * smaller deformation. In short, large number = small deformation.
	 * 
	 * The vector method returns the momentary data. It is recommended to call 
	 * this method continuously, for example in the draw() function of the
	 * parent Processing sketch, as this function is executed continuously. This 
	 * implementation will result in a continuous signal.
	 * 
	 * @return momentary vector
	 * @see vector(int)
	 * @see raw()
	 * @see mag()
	 * @see dir()
	 */
	public static float[] vector() {
		return instance.vector.array();
	}
	
	
	/**
	 * Sensed deformation related to the specified electrode, presented as
	 * a multidimensional vector. The subvector is constructed from the
	 * pre-processed raw measurement. All values are scaled to a range 0–1
	 * and reversed. Each vector component represents the measured voltage (thus 
	 * electrical resistance, thus distance, thus deformation).
	 * 
	 * Note that a bigger value means a bigger deformation. A bigger value is 
	 * derived from a smaller measured voltage, as a result of a bigger
	 * electrical resistance (this inverse is caused by the voltage divider). 
	 * A bigger electrical resistance means a larger distance, and thus a
	 * smaller deformation. In short, large number = small deformation.
	 * 
	 * The vector method returns the momentary data. It is recommended to call 
	 * this method continuously, for example in the draw() function of the
	 * parent Processing sketch, as this function is executed continuously. This 
	 * implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary vector
	 * @see vector()
	 * @see raw(int)
	 * @see mag(int)
	 * @see dir(int)
	 */
	public static float[] vector(int e) {
		if(instance.shield) {
			return instance.subvector(instance.vector, e).array();
		}
		return new float[] {0};
	}
	
	
	/*
	 * Creates subvector from given vector of corresponding electrode.
	 * 
	 * While vector(int) returns the subvector of specified electrode 
	 * constructed from the momentary vector, this subvector method allows to
	 * construct the subvector from any given vector (for example to  extract 
	 * subvectors from slidingWindow data).
	 * 
	 * As this method is not dependent from momentary values, the resulting 
	 * vector is not necessarily momentary. Meaning, the resulting signal is 
	 * only continuous if called continuously AND with a continous parameter v.
	 * 
	 * This is a private method to reduce complexity of the offered features.
	 * 
	 * @param v  vector
	 * @param e  elecrode
	 * @return subvector
	 * @see vector(int)
	 * @see avg(int)
	 * @see stdev(int)
	 * @see diff(int)
	 */
	private Vector subvector(Vector v, int e) {
		if(v.dim() == 28) {
			float[] sub = new float[] { 0, 0, 0, 0, 0, 0, 0 }; 
			for(int i = 0; i < 7; i++) {
				// for each other electrode
				// retrieve corresponding vector component
					sub[i] = v.x(SUBVECTOR_MAP[e][i]);
				}
			return new Vector(sub);
		}
		return new Vector(new float[] {0});
	}
	
	/**
	 * Momentary vector magnitude, considering the measured deformation as
	 * multidimensional vector (taking the measured values in one cycle as
	 * vector components).
	 * 
	 * Note that the vector magnitude represents the length of the vector. The
	 * magnitude of the vector can be used to quantify the deformation in a
	 * single number, representing a measure of deformation for example
	 * deformation 'amplitude', 'depth', or 'intensity'.
	 * 
	 * As the magnitude method is based on the momentary vector, it is
	 * recommended to call this method continuously, for example in the draw() 
	 * function of the parent Processing sketch, as this function is executed 
	 * continuously. This implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary magnitude (vector length)
	 * @see mag(int)
	 * @see vector()
	 * @see dir()
	 * @see max()
	 * @see avg()
	 * @see stdev()
	 * @see diff()
	 */
	public static float mag() {
		return instance.mag;
	}
	
	/**
	 * Momentary magnitude of the subvector corresponding with the specified
	 * electrode, considering the measured deformation as multidimensional 
	 * vector (taking the measured values in one cycle as vector components).
	 * 
	 * Note that the vector magnitude represents the length of the vector. The
	 * magnitude of the vector can be used to quantify the deformation in a
	 * single number, representing a measure of deformation for example
	 * deformation 'amplitude', 'depth', or 'intensity'.
	 * 
	 * As the magnitude method is based on the momentary vector, it is
	 * recommended to call this method continuously, for example in the draw() 
	 * function of the parent Processing sketch, as this function is executed 
	 * continuously. This implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary magnitude (vector length)
	 * @see mag()
	 * @see vector(int)
	 * @see dir(int)
	 * @see max(int)
	 * @see avg(int)
	 * @see stdev(int)
	 * @see diff(int)
	 */
	public static float mag(int e) {
		return instance.submag[e];
	}
	
	/**
	 * Momentary vector direction, considering the measured deformation as
	 * multidimensional vector (taking the measured values in one cycle as
	 * vector components).
	 * 
	 * Note that the vector direction (or unit vector) has a magnitude of '1'. 
	 * The direction of the vector can for example be used for form recognition.
	 * 
	 * As the direction method is based on the momentary vector, it is
	 * recommended to call this method continuously, for example in the draw() 
	 * function of the parent Processing sketch, as this function is executed 
	 * continuously. This implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary direction (unit vector)
	 * @see dir(int)
	 * @see vector()
	 * @see mag()
	 * @see record()
	 * @see record(String)
	 * @see recog()
	 * @see recog(String)
	 */
	public static float[] dir() {
		return instance.vector.dir();
	}
	
	
	/**
	 * Momentary direction of the subvector corresponding with the specified
	 * electrode, considering the measured deformation as multidimensional
	 * vector (taking the measured values in one cycle as vector components). 
	 * 
	 * Note that the vector direction (or unit vector) has a magnitude of '1'.
	 * The direction of the vector can for example be used for form recognition.
	 * 
	 * As the direction method is based on the momentary vector, it is
	 * recommended to call this method continuously, for example in the draw() 
	 * function of the parent Processing sketch, as this function is executed 
	 * continuously. This implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary direction (unit vector)
	 * @see dir()
	 * @see vector(int)
	 * @see mag(int)
	 */
	public static float[] dir(int e) {
		return Vector.dir(vector(e));
	}

	
/*	TIME SERIES ANALYSIS */	
	
	/**
	 * Maximum observed momentary vector magnitude. This feature thus offers a 
	 * continuous signal of the maximum observed value up until that moment.
	 * This signal is useful for data normalization or feature scaling (map a 
	 * signal from the range 0–MAX to the range 0–1).
	 * 
	 * As the max method is based on the momentary vector, it is recommended to 
	 * call this method continuously, for example in the draw() function of the 
	 * parent Processing sketch, as this function is executed continuously. This 
	 * implementation will result in a continuous signal.
	 * 
	 * @return momentary maximum
	 * @see max(int)
	 * @see mag()
	 * @see norm()
	 * @see avg()
	 * @see stdev()
	 * @see diff()
	 */
	public static float max() {
		return instance.max;
	}
	
	
	/**
	 * Maximum observed momentary magnitude of the subvector corresponding with 
	 * the specified electrode. This feature thus offers a continuous signal of 
	 * the maximum observed value up until that moment. This signal is useful
	 * for data normalization or feature scaling (map a signal from the range
	 * 0–MAX to the range 0–1).
	 * 
	 * As the max method is based on the momentary vector, it is recommended to 
	 * call this method continuously, for example in the draw() function of the 
	 * parent Processing sketch, as this function is executed continuously. This 
	 * implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary maximum
	 * @see max()
	 * @see mag(int)
	 * @see norm(int)
	 * @see avg(int)
	 * @see stdev(int)
	 * @see diff(int)
	 */
	public static float max(int e) {
		return instance.submax[e];
	}
	
	/**
	 * Momentary moving average of vector magnitude. This feature thus offers a 
	 * continuous signal of the moving average, a smoothed signal.
	 * 
	 * As the average method is based on the momentary vector, it is recommended
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously. 
	 * This implementation will result in a continuous signal.
	 * 
	 * @return momentary moving average
	 * @see avg(int)
	 * @see mag()
	 * @see norm()
	 * @see max()
	 * @see stdev()
	 * @see diff()
	 */
	public static float avg() {
		if (instance.running) {
			// if slidingWindow contains data
			float sum = 0;
			int n = instance.window.length;
			for(int i = 0; i < instance.window.length; i++) {
				// for each timepoint (a vector) in slidingWindow
				// sum its magnitude
				if(instance.window[i] != null) {
					sum += instance.window[i].mag();
				} else {
					n--;
				}
			}
			// divide sum by number of timepoints (length of slidingWindow)
			return sum/n; 
		} else {
			return 0;  // empty slidingWindow
		}
	}
	
	
	/**
	 * Momentary moving average of the magnitude of the subvector corresponding 
	 * with the specified electrode. This feature thus offers a continuous
	 * signal of the moving average, a smoothed signal.
	 * 
	 * As the average method is based on the momentary vector, it is recommended
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously. 
	 * This implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary moving average
	 * @see avg()
	 * @see mag(int)
	 * @see norm(int)
	 * @see max(int)
	 * @see stdev(int)
	 * @see diff(int)
	 */
	public static float avg(int e) {
		if (instance.shield) {
			// if slidingWindow contains data
			float sum = 0;
			int n = instance.window.length;
			for(int i = 0; i < instance.window.length; i++) {
				// for each timepoint (a vector) in slidingWindow
				// construct subvector
				if(instance.window[i] != null) {
					Vector sub = instance.subvector(instance.window[i], e);
					// sum subvector magnitude
					sum += sub.mag();
				} else {
					n--;
				}
			}
			// divide sum by number of timepoints (length of slidingWindow)
			return sum/n;
		} else {
			return 0;  // empty slidingWindow
		}
	}
	
	
	/**
	 * Momentary moving standard deviation of vector magnitude. This feature 
	 * thus offers a continuous signal of the moving standard deviation.
	 * 
	 * As the standard deviation method is based on the momentary vector, it is 
	 * recommended to call this method continuously, for example in the draw() 
	 * function of the parent Processing sketch, as this function is executed 
	 * continuously. This implementation will result in a continuous signal.
	 * 
	 * @return momentary moving standard deviation
	 * @see stdev(int)
	 * @see mag()
	 * @see max()
	 * @see avg()
	 * @see diff()
	 */
	public static float stdev() {
		if (instance.running) {
			// if slidingWindow contains data
			float avg = avg();
			float var = 0;
			int n = instance.window.length;
			for(int i = 0; i < instance.window.length; i++) {
				// for each timepoint (a vector) in slidingWindow
				// sum squared distance from its magnitude to avg
				if(instance.window[i] != null) {
					float d = instance.window[i].mag() - avg;
					var += d*d;
				} else {
					n--;
				}
			}
			// divide variance by number of timepoints (length of slidingWindow)
			// and take square root
			return (float) Math.sqrt(var/n); 
		} else {
			return 0;  // empty slidingWindow
		}
	}
	
	
	/**
	 * Momentary moving standard deviation of the magnitude of the subvector 
	 * corresponding with the specified electrode. This feature thus offers a 
	 * continuous signal of the moving standard deviation.
	 * 
	 * As the standard deviation method is based on the momentary vector, it is 
	 * recommended to call this method continuously, for example in the draw() 
	 * function of the parent Processing sketch, as this function is executed 
	 * continuously. This implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary moving standard deviation
	 * @see stdev()
	 * @see mag(int)
	 * @see max(int)
	 * @see avg(int)
	 * @see diff(int)
	 */
	public static float stdev(int e) {
		if (instance.shield) {
			// if slidingWindow contains data
			float avg = avg(e);
			float var = 0;
			int n = instance.window.length;
			for(int i = 0; i < instance.window.length; i++) {
				// for each timepoint (a vector) in slidingWindow
				// construct subvector, calculate distance to avg, and add 
				// squared distance to variance
				if(instance.window[i] != null) {
					Vector sub = instance.subvector(instance.window[i], e);
					float d = sub.mag() - avg;
					var += d*d;
				} else {
					n--;
				}
			}
			// divide variance by number of timepoints (length of slidingWindow)
			// and take square root
			return (float) Math.sqrt(var/n);
		} else {
			return 0;  // empty slidingWindow
		}
	}
	
	
	/**
	 * Momentary differentiation (first derivative) of vector magnitude. This 
	 * feature thus offers a continuous signal of the rate of change. Its value 
	 * can be interpreted as the speed of change, while its sign carries meaning
	 * regarding the direction of change (becoming smaller or bigger).
	 * 
	 * As the diff method is based on the momentary vector, it is recommended
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously. 
	 * This implementation will result in a continuous signal.
	 * 
	 * @return momentary moving average
	 * @see diff(int)
	 * @see mag()
	 * @see max()
	 * @see avg()
	 * @see stdev()
	 */
	public static float diff() {
		int[] w = new int[] { -1, 8, 0, -8, 1};  // formula weights of timepoints
		// if slidingWindow contains at least 5 data points
		// approximate first derivative using five-point stencil formula
		if (instance.running) {
			if(instance.window.length > 4) {
				if(instance.window[4] != null) {
					float sum = w[0]*instance.window[0].mag();
					sum += w[1]*instance.window[1].mag();
					sum += w[3]*instance.window[3].mag();
					sum += w[4]*instance.window[4].mag();
					return sum/12;
				}
			}	
		}
		return 0;  // slidingWindow does not fit formula
	}
	
	
	/**
	 * Momentary differentiation (first derivative) of the magnitude of the
	 * subvector corresponding with the specified electrode.  This feature thus 
	 * offers a continuous signal of the rate of change. Its value can be
	 * interpreted as the speed of change, while its sign carries meaning
	 * regarding the direction of change (becoming smaller or bigger).
	 * 
	 * As the diff method is based on the momentary vector, it is recommended
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously. 
	 * This implementation will result in a continuous signal.
	 * 
	 * @return momentary moving average
	 * @see diff()
	 * @see mag(int)
	 * @see max(int)
	 * @see avg(int)
	 * @see stdev(int)
	 */
	public static float diff(int e) {
		int[] w = new int[] { -1, 8, 0, -8, 1};  // formula weights of timepoints
		if (instance.shield) {
			if(instance.window.length > 4) {
				if(instance.window[4] != null) {
					// if slidingWindow contains at least 5 data points
					// approximate first derivative using five-point stencil formula
					float sum = w[0]*instance.subvector(instance.window[0], e).mag();
					sum += w[1]*instance.subvector(instance.window[1], e).mag();
					sum += w[3]*instance.subvector(instance.window[3], e).mag();
					sum += w[4]*instance.subvector(instance.window[4], e).mag();
					return sum/12;
				}
			}
		}
		return 0;  // slidingWindow does not fit formula
	}
	
	
/*	SIGNAL TRANSFORMATION */	
	
	/**
	 * Normalized, moving average of the momentary vector magnitude. This
	 * feature thus offers a continuous, smoothed signal with values in the
	 * range 0–1.
	 * 
	 * Normalization scales the momentary moving average within the known range
	 * of values, meaning the normalized value brings the range of values 0–1.
	 * This range refers to the absolute values 0–MAX.
	 * 
	 * As the norm method is based on the momentary vector, it is recommended 
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously.
	 * This implementation will result in a continuous signal.
	 * 
	 * @return momentary normal
	 * @see norm(int)
	 * @see avg()
	 * @see max()
	 * @see inv()
	 * @see square()
	 * @see root()
	 */
	public static float norm() {
		if(instance.running) {
			return avg()/instance.max;  // TODO independent of over-/undershoot?
		}
		return 0;
	}
	
	
	/**
	 * Normalized, moving average of the momentary magnitude of the subvector 
	 * corresponding with the specified electrode. This feature thus offers a 
	 * continuous, smoothed signal with values in the range 0–1.
	 * 
	 * Normalization scales the momentary moving average within the known range
	 * of values, meaning the normalized value brings the range of values 0–1.
	 * This range refers to the absolute values 0–MAX.
	 * 
	 * As the norm method is based on the momentary vector, it is recommended 
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously.
	 * This implementation will result in a continuous signal.
	 * 
	 * @return momentary normal
	 * @see norm()
	 * @see avg(int)
	 * @see max(int)
	 * @see inv(int)
	 * @see square(int)
	 * @see root(int)
	 */
	public static float norm(int e) {
		if(instance.shield) {
			return avg(e)/instance.submax[e];  // TODO independent of over-/undershoot?
		}
		return 0;
	}
	
	
	/**
	 * Inverse, normalized, moving average of the momentary vector magnitude. 
	 * This feature thus offers a continuous, smoothed signal with values in 
	 * the range 0–1.
	 * 
	 * As the inverse method is based on the momentary vector, it is recommended 
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously.
	 * This implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary inverse
	 * @see inv(int)
	 * @see norm()
	 * @see square()
	 * @see root()
	 */
	public static float inv() {
		return 1-norm();
	}
	
	
	/**
	 * Inverse, normalized, moving average of the momentary magnitude of the
	 * subvector corresponding with the specified electrode. This feature thus 
	 * offers a continuous, smoothed signal with values in the range 0–1.
	 * 
	 * As the inverse method is based on the momentary vector, it is recommended 
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously.
	 * This implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary inverse
	 * @see inv()
	 * @see norm(int)
	 * @see square(int)
	 * @see root(int)
	 */
	public static float inv(int e) {
		return 1-norm(e);
	}
	
	/**
	 * Squared, normalized, moving average of the momentary vector magnitude. 
	 * This feature thus offers a continuous, smoothed signal with values in the 
	 * range 0–1.
	 * 
	 * Squaring emphasizes big numbers, meaning that this feature can be used 
	 * for coupling that allows nuance of strong deformations.
	 * 
	 * As the square method is based on the momentary vector, it is recommended 
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously.
	 * This implementation will result in a continuous signal.
	 * 
	 * @return momentary square
	 * @see square(int)
	 * @see norm()
	 * @see inv()
	 * @see root()
	 */
	public static float square() {
		float x = norm();
		return x*x;
	}
	
	/**
	 * Squared, normalized, moving average of the momentary magnitude of the
	 * subvector corresponding with the specified electrode. This feature thus 
	 * offers a continuous, smoothed signal with values in the range 0–1.
	 * 
	 * Squaring emphasizes big numbers, meaning that this feature can be used 
	 * for coupling that allows nuance of strong deformations.
	 * 
	 * As the square method is based on the momentary vector, it is recommended 
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously.
	 * This implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary square
	 * @see square()
	 * @see norm(int)
	 * @see inv(int)
	 * @see root(int)
	 */
	public static float square(int e) {
		float x = norm(e);
		return x*x;
	}
	
	/**
	 * Square root of the normalized, moving average of the momentary vector 
	 * magnitude. This feature thus offers a continuous, smoothed signal with
	 * values in the range 0–1.
	 * 
	 * The square root emphasizes small numbers, meaning that this feature can 
	 * be used for coupling that allows nuance  of light deformations.
	 * 
	 * As the root method is based on the momentary vector, it is recommended 
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously.
	 * This implementation will result in a continuous signal.
	 * 
	 * @return momentary square root
	 * @see root(int)
	 * @see norm()
	 * @see inv()
	 * @see square()
	 */
	public static float root() {
		return (float) Math.sqrt(norm());
	}
	
	
	/**
	 * Square root of the normalized, moving average of the momentary magnitude
	 * of the subvector corresponding with the specified electrode. This feature
	 * thus offers a continuous, smoothed signal with values in the range 0–1.
	 * 
	 * The square root emphasizes small numbers, meaning that this feature can 
	 * be used for coupling that allows nuance of light deformations.
	 * 
	 * As the root method is based on the momentary vector, it is recommended 
	 * to call this method continuously, for example in the draw() function of
	 * the parent Processing sketch, as this function is executed continuously.
	 * This implementation will result in a continuous signal.
	 * 
	 * @param e  electrode
	 * @return momentary square root
	 * @see root()
	 * @see norm(int)
	 * @see inv(int)
	 * @see square(int)
	 */
	public static float root(int e) {
		return (float) Math.sqrt(norm(e));
	}

	
/*	FORM RECOGNITION */	
	
	/**
	 * Record: stores the momentary form (taking the vector direction as sample)
	 * unlabeled, can be used in combination with the recognition function (rcg).
	 * 
	 * Note that the unlabeled form equals the label "", as recorded through 
	 * rcd(""). If rcd() and/or rcd("") are called consecutively or repeatedly, 
	 * the same form will be overwritten, meaning the most recent call will
	 * define the actual direction sample.
	 * 
	 * As the record function stores a sample or snapshot of the momentary
	 * vector, it is recommended to avoid calling this method continuously in
	 * order to avoid undesired overwrites. Calling this method outside draw()
	 * function of the parent Processing sketch, will prevent this (mostly)
	 * undesired behavior as the draw() function is executed continuously.
	 * 
	 * @see record(String)
	 * @see dir()
	 * @see recog()
	 */
	public static void rcd() {
		rcd("");  // unlabeled form equals label ""
	}
	
	
	/**
	 * Record: stores the momentary form (taking the vector direction as sample)
	 * with specified label, can be used in combination with the recognition
	 * function (rcg).
	 * 
	 * Note that repeated calls with the same argument will overwrite preceding
	 * calls, meaning the most recent call will define the actual direction sample.
	 * 
	 * Note that the label "" equals the unlabeled form, as recorded through 
	 * rcd(). If rcd() and/or rcd("") are called consecutively or repeatedly, 
	 * the same form will be overwritten, meaning the most recent call will
	 * define the actual direction sample.
	 * 
	 * As the record function stores a sample or snapshot of the momentary
	 * vector, it is recommended to avoid calling this method continuously in
	 * order to avoid undesired overwrites. Calling this method outside draw()
	 * function of the parent Processing sketch, will prevent this (mostly)
	 * undesired behavior as the draw() function is executed continuously.
	 * 
	 * @param form  label
	 * @see record()
	 * @see dir()
	 * @see recog(String)
	 */
	public static void rcd(String f) {
		if(instance.running) {
			// TODO split?
			Boolean found = false;  // to check if specified form exists already
			// check whether form exists already, and re-record if so
			for(Form form : instance.forms) {
				// for each recorded form in 'forms'
				if (form.is(f)) {
					// if form label matches specified label
					// then record form (overwrites existing with current direction)
					form.rcd(instance.vector);
					found = true;
					break;
				}
			}
			// create and record new form if it does not exist yet
			if (!found) {
				Form form = new Form(f, instance.vector);
				instance.forms.add(form);
			}
		}
	}
	
	
	/**
	 * Returns set of recorded Skweezee forms, but labeled and unlabeled. The
	 * resulting List contains Form objects.
	 * 
	 * @return List of Form objects
	 * @see record()
	 * @see dir()
	 * @see record(String)
	 */
	public static List<Form> forms() {
		return instance.forms;
	}
	
	
	/**
	 * Recognize function: compares the current and recorded unlabeled form,
	 * to be used in combination with the record function. Using the dot product
	 * between the two directions. The resulting number, range 0–1, represents
	 * the match.
	 * 
	 * As the recognize function is based on the momentary vector, it is 
	 * recommended to call this method continuously, for example in the draw()
	 * function of the parent Processing sketch, as this function is executed
	 * continuously. This implementation will result in a continuous signal.
	 * 
	 * @return momentary match between the current and recorded form
	 * @see record()
	 * @see dir()
	 * @see recog(String)
	 */
	public static float rcg() {
		return rcg("");  // unlabeled form equals label ""
	}
	
	
	/**
	 * Recognize function: compares the current and recorded form with specified
	 * label), to be used in combination with the record function. Using the dot
	 * product between the two directions. The resulting number, range 0–1,
	 * represents the match.
	 * 
	 * As the recognize function is based on the momentary vector, it is 
	 * recommended to call this method continuously, for example in the draw()
	 * function of the parent Processing sketch, as this function is executed
	 * continuously. This implementation will result in a continuous signal.
	 * 
	 * @param form  label
	 * @return momentary match between current and recorded form
	 * @see record(String)
	 * @see dir()
	 * @see recog()
	 */
	public static float rcg(String f) {
		for (Form form : instance.forms) {
			// for each recorded form in 'forms'
			if (form.is(f)) {
				// if form label matches specified label
				// then return fit with current direction (via dot product)
				return form.rcg();
			}
		}
		return 0;  // when label was not found (requested form does not exist)
	}
}

/*	TODO */

//	replace 'function' with 'method' in javadoc
//	Check effect of broken connections on magnitude/direction?	
//	Verbose mode?	
//	Split methods?
//	Add public reset (small or 'soft' reset) (related to max & recorded forms)	
//	MAX > since? ever? last reset? last peak?
//	Define max on baseline? (overshoot? via stdev?)
//	Check sliding window (currently fixed at n=5)
// 	Check role of magnitude in recording forms?
//	Add peak detection, frequency?
// 	Add histogram? > find baselines in directions

/* 	Check: From Processing documentation: Using a static initializer such as
 * 	javaYourLibrary.init(this) is discouraged because it's not a good way to
 * 	write OOP code, and makes assumptions about how your library connects to a
 * 	sketch, and it encourages bad style that implies a single instance of your
 * 	library. If you need to share information across library instances, use
 * 	static variables or methods behind the scenes, but don't expose them to the
 * 	user through the initializer.
 *  
 * 	https://github.com/processing/processing/wiki/Library-Basics
 */ 

// 80 COLS ------------------------------------------------------------------- X
// 65 COLS ---------------------------------------------------- X